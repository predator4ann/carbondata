/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.command.stream

import scala.collection.JavaConverters._

import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.command.DataCommand
import org.apache.spark.sql.execution.datasources.LogicalRelation
import org.apache.spark.sql.types.{StringType, StructType}

import org.apache.carbondata.common.exceptions.sql.MalformedCarbonCommandException
import org.apache.carbondata.core.metadata.schema.table.CarbonTable
import org.apache.carbondata.spark.StreamingOption
import org.apache.carbondata.spark.util.Util
import org.apache.carbondata.stream.StreamJobManager

/**
 * This command will start a Spark streaming job to insert rows from source to sink
 */
case class CarbonCreateStreamCommand(
    streamName: String,
    sinkDbName: Option[String],
    sinkTableName: String,
    ifNotExists: Boolean,
    optionMap: Map[String, String],
    query: String
) extends DataCommand {

  override def output: Seq[Attribute] =
    Seq(AttributeReference("Stream Name", StringType, nullable = false)(),
      AttributeReference("JobId", StringType, nullable = false)(),
      AttributeReference("Status", StringType, nullable = false)())

  override def processData(sparkSession: SparkSession): Seq[Row] = {
    val df = sparkSession.sql(query)
    var sourceTable: CarbonTable = null
    var dataFrame: Option[DataFrame] = None

    // Prepare the dataframe from the stream source table
    df.logicalPlan transform {
      case r: LogicalRelation
        if r.relation.isInstanceOf[CarbonDatasourceHadoopRelation] &&
           r.relation.asInstanceOf[CarbonDatasourceHadoopRelation].carbonTable.isStreamingSource =>
        val (source, resolvedFrame) = prepareDataFrame(sparkSession, r)
        if (sourceTable != null && sourceTable.getTableName != source.getTableName) {
          throw new MalformedCarbonCommandException(
            "Stream query on more than one stream source table is not supported")
        }
        sourceTable = source
        dataFrame = Option(resolvedFrame)
        r
      case plan: LogicalPlan => plan
    }

    if (sourceTable == null) {
      throw new MalformedCarbonCommandException("Must specify stream source table in the query")
    }

    // start the streaming job
    val jobId = StreamJobManager.startStream(
      sparkSession = sparkSession,
      ifNotExists = ifNotExists,
      streamName = streamName,
      sourceTable = sourceTable,
      sinkTable = CarbonEnv.getCarbonTable(sinkDbName, sinkTableName)(sparkSession),
      query = query,
      streamDf = dataFrame.getOrElse(Dataset.ofRows(sparkSession, df.logicalPlan)),
      options = new StreamingOption(optionMap)
    )
    Seq(Row(streamName, jobId, "RUNNING"))
  }

  /**
   * Create a dataframe from source table of logicalRelation
   * @param sparkSession
   * @param logicalRelation
   * @return sourceTable and its stream dataFrame
   */
  private def prepareDataFrame(
      sparkSession: SparkSession,
      logicalRelation: LogicalRelation): (CarbonTable, DataFrame) = {
    val sourceTable = logicalRelation.relation
      .asInstanceOf[CarbonDatasourceHadoopRelation].carbonTable
    val tblProperty = sourceTable.getTableInfo.getFactTable.getTableProperties
    val format = tblProperty.get("format")
    if (format == null) {
      throw new MalformedCarbonCommandException("Streaming from carbon file is not supported")
    }
    val streamReader = if (format.equalsIgnoreCase("kafka") ||
                           format.equalsIgnoreCase("dis")) {
      // kafka source fixed schema, it cannot be set to a custom schema
      sparkSession.readStream
        .format(format)

    } else {
      sparkSession.readStream
        .schema(getSparkSchema(sourceTable))
        .format(format)
    }
    val dataFrame = format match {
      case "csv" | "text" | "json" | "parquet" =>
        if (!tblProperty.containsKey("path")) {
          throw new MalformedCarbonCommandException(
            s"'path' tblproperty should be provided for '$format' format")
        }
        streamReader.load(tblProperty.get("path"))
      case "kafka" | "socket" | "dis" =>
        // get source information from SparkSession
        sparkSession.conf.getAll.foreach { entry =>
          if (entry._1.toLowerCase.startsWith("carbon.source.")) {
            streamReader.option(entry._1.substring("carbon.source.".length), entry._2)
          }
        }
        streamReader.options(tblProperty).load()
      case other =>
        throw new MalformedCarbonCommandException(s"Streaming from $format is not supported")
    }
    (sourceTable, dataFrame)
  }

  private def getSparkSchema(sourceTable: CarbonTable): StructType = {
    val cols = sourceTable.getTableInfo.getFactTable.getListOfColumns.asScala.toArray
    val sortedCols = cols.filter(_.getSchemaOrdinal != -1)
      .sortWith(_.getSchemaOrdinal < _.getSchemaOrdinal)
    Util.convertToSparkSchema(sourceTable, sortedCols)
  }

}
