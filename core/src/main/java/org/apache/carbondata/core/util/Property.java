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

package org.apache.carbondata.core.util;

public class Property<T> {

  private String key;
  private T value;
  private T defaultValue;
  private String doc;
  private boolean dynamicConfigurable;

  public Property(String key, T defaultValue, String doc, boolean dynamicConfigurable) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.doc = doc;
    this.dynamicConfigurable = dynamicConfigurable;
  }

  public String getKey() {
    return key;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public String getDefaultValueString() {
    return String.valueOf(defaultValue);
  }

  public static PropertyBuilder<String> buildStringProperty() {
    return new PropertyBuilder<String>();
  }

  public static PropertyBuilder<Boolean> buildBooleanProperty() {
    return new PropertyBuilder<Boolean>();
  }
}