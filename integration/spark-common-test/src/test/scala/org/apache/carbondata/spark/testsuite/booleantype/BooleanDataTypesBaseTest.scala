package org.apache.carbondata.spark.testsuite.booleantype

import org.apache.spark.sql.Row
import org.apache.spark.sql.test.util.QueryTest
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

/**
  * Created by root on 9/17/17.
  */
class BooleanDataTypesBaseTest extends QueryTest with BeforeAndAfterEach with BeforeAndAfterAll {

  override def beforeEach(): Unit = {
    sql("drop table if exists carbon_table")
    sql("drop table if exists boolean_table")
  }

  override def afterEach(): Unit = {
    sql("drop table if exists carbon_table")
    sql("drop table if exists boolean_table")
  }

  test("Creating table: boolean one column, should support") {
    try {
      sql("CREATE TABLE if not exists boolean_table(cc BOOLEAN) STORED BY 'carbondata'")
      assert(true)
    } catch {
      case _: Exception => assert(false)
    }
  }

  test("Creating table: boolean and other table, should support") {
    try {
      sql(
        s"""
           |CREATE TABLE if not exists boolean_table(
           |aa INT, bb STRING, cc BOOLEAN
           |) STORED BY 'carbondata'""".stripMargin)
      assert(true)
    } catch {
      case _: Exception => assert(false)
    }
  }

  test("Describing table: boolean data type, should support") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |cc BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    checkExistence(sql("describe formatted carbon_table"), true, "boolean")
  }

  test("Describing table: support boolean data type format and other table ") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         | shortField SHORT,
         | intField INT,
         | bigintField LONG,
         | doubleField DOUBLE,
         | stringField STRING,
         | timestampField TIMESTAMP,
         | decimalField DECIMAL(18,2),
         | dateField DATE,
         | charField CHAR(5),
         | floatField FLOAT,
         | complexData ARRAY<STRING>,
         | booleanField BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    checkExistence(sql("describe formatted carbon_table"), true, "boolean")
  }

  test("Altering table and add column: add boolean type column") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |aa INT, bb STRING
         |) STORED BY 'carbondata'""".stripMargin)
    sql("alter table carbon_table add columns (dd BOOLEAN)")
    checkExistence(sql("describe formatted carbon_table"), true, "boolean")
    checkExistence(sql("describe formatted carbon_table"), true, "dd")
  }

  test("Altering table and add column: exists boolean column, add boolean type column") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |aa INT, bb STRING, cc BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    sql("alter table carbon_table add columns (dd BOOLEAN)")
    checkExistence(sql("describe formatted carbon_table"), true, "boolean")
    checkExistence(sql("describe formatted carbon_table"), true, "dd")
  }

  test("Altering table and add column: exists boolean column, add not boolean type column") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |aa INT, bb STRING, cc BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    sql("alter table carbon_table add columns (dd STRING)")
    checkExistence(sql("describe formatted carbon_table"), true, "boolean")
    checkExistence(sql("describe formatted carbon_table"), true, "dd")
  }

  test("Altering table and add column and insert values: exists boolean column, add boolean type column") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |aa STRING, bb INT, cc BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    sql("alter table carbon_table add columns (dd BOOLEAN)")
    sql("insert into carbon_table values('adam',11,true,false)")
    checkAnswer(sql("select * from carbon_table"), Seq(Row("adam", 11, true, false)))
  }

  test("Altering table and drop column and insert values: exists boolean column, add boolean type column") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |aa STRING, bb INT, cc BOOLEAN, dd BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    sql("alter table carbon_table drop columns (dd)")
    sql("insert into carbon_table values('adam',11,true)")
    checkAnswer(sql("select * from carbon_table"), Seq(Row("adam", 11, true)))
  }

  test("Deleting table and drop column and insert values: exists boolean column, add boolean type column") {
    sql(
      s"""
         |CREATE TABLE if not exists carbon_table(
         |aa STRING, bb INT, cc BOOLEAN, dd BOOLEAN
         |) STORED BY 'carbondata'""".stripMargin)
    sql("alter table carbon_table drop columns (dd)")
    sql("insert into carbon_table values('adam',11,true)")
    checkAnswer(sql("select * from carbon_table"), Seq(Row("adam", 11, true)))
    sql("delete from carbon_table where cc=true")
    checkAnswer(sql("select COUNT(*) from carbon_table"), Row(0))
  }
}
