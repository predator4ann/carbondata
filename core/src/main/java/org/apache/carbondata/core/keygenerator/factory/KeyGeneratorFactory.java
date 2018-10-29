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

package org.apache.carbondata.core.keygenerator.factory;

import org.apache.carbondata.core.constants.CarbonCommonConstants;
import org.apache.carbondata.core.keygenerator.KeyGenerator;
import org.apache.carbondata.core.keygenerator.mdkey.MultiDimKeyVarLengthGenerator;
import org.apache.carbondata.core.util.CarbonUtil;

public final class KeyGeneratorFactory {
  private KeyGeneratorFactory() {

  }

  public static KeyGenerator getKeyGenerator(int[] dimension) {
    int[] incrementedCardinality;
    boolean isFullyFilled =
        Boolean.parseBoolean(CarbonCommonConstants.IS_FULLY_FILLED_BITS_DEFAULT);
    if (!isFullyFilled) {
      incrementedCardinality = CarbonUtil.getIncrementedCardinality(dimension);
    } else {
      incrementedCardinality = CarbonUtil.getIncrementedCardinalityFullyFilled(dimension);
    }
    return new MultiDimKeyVarLengthGenerator(incrementedCardinality);
  }

  /**
   *
   * @param dimCardinality : dimension cardinality
   * @param columnSplits : No of column in each block
   * @return keygenerator
   */
  public static KeyGenerator getKeyGenerator(int[] dimCardinality, int[] columnSplits) {
    int[] dimsBitLens = CarbonUtil.getDimensionBitLength(dimCardinality, columnSplits);

    return new MultiDimKeyVarLengthGenerator(dimsBitLens);
  }
}

