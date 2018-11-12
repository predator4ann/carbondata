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

import java.util.Objects;

public class PropertyBuilder<T> {

  private String key;
  private T defaultValue;
  private String doc;
  private boolean dynamicConfigurable;

  public PropertyBuilder<T> key(String key) {
    Objects.requireNonNull(key, "path should not be null");
    this.key = key;
    return this;
  }

  public PropertyBuilder<T> defaultValue(T defaultValue) {
    Objects.requireNonNull(defaultValue, "defaultValue should not be null");
    this.defaultValue = defaultValue;
    return this;
  }

  public PropertyBuilder<T> doc(String doc) {
    Objects.requireNonNull(doc, "doc should not be null");
    this.doc = doc;
    return this;
  }

  public PropertyBuilder<T> dynamicConfigurable(boolean dynamicConfigurable) {
    this.dynamicConfigurable = dynamicConfigurable;
    return this;
  }

  public Property<T> build() {
    Objects.requireNonNull(this.key, "dynamicConfigurable should not be null");
    Objects.requireNonNull(this.defaultValue, "defaultValue should not be null");
    Objects.requireNonNull(this.doc, "doc should not be null");
    return new Property<T>(key, defaultValue, doc, dynamicConfigurable);
  }
}
