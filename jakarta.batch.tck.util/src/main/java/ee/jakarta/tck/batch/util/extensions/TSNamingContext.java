/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ee.jakarta.tck.batch.util.extensions;

import java.util.Properties;
import javax.naming.InitialContext;

/**
 * TSNamingContext provides a wrapper for all lookups.
 */
public class TSNamingContext {

  Properties props = null;

  public TSNamingContext() {
  }

  public TSNamingContext(Properties pp) {
    if (pp != null) {
      props = pp;
    }
  }

  /**
   * Provides lookup of an object.
   *
   * @param s
   *          object name to lookup
   * @param c
   *          object class to narrow to
   */
  public <T> T lookup(String s, Class<T> c) throws Exception {
    Object o = lookup(s);
    if (c != null && c.isAssignableFrom(o.getClass())) {
        return c.cast(o);
    } else {
        return null;
    }
  }

  /**
   * Provides lookup of an object.
   *
   * @param s
   *          object name to lookup
   */
  public Object lookup(String s) throws Exception {
    if (props != null) {
      return new InitialContext(props).lookup(s);
    } else {
      return new InitialContext().lookup(s);
    }
  }

}
