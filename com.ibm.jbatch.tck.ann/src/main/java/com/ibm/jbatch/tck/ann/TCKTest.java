/*
 * Copyright 2015, 2020 International Business Machines Corp. and others
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.jbatch.tck.ann;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The TCKTest annotation is used to generate a report of the TCK coverage.
 * <br><br>
 * <span style='font-weight:bold;'> Required Attributes: </span> versions, assertions, specRefs
 * <br>
 * Optional Attributes: apiRefs, issueRefs, strategy, notes
 */
@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface TCKTest {

    /**
     * The versions of the TCK that the test was updated for.
     * <br><br>
     * <span style='font-weight:bold;'> versions[0] should contain the first version of the TCK that included this test. </span>
     * <br><br>
     * List of valid values:
     * <ul>
     *   <li>1.0</li>
     * </ul>
     */
    String[] versions();

    /**
     * The behaviors being tested
     */
    String[] assertions();

    /**
     * References to the Jakarta Batch sections that specify the behavior being tested
     */
    SpecRef[] specRefs();

    /**
     * References to the Jakarta Batch APIs being tested
     */
    APIRef[] apiRefs() default {};

    /**
     * URLs referencing bugs the test was added for
     */
    String[] issueRefs() default {};

    /**
     * The strategy used to test the assertions
     */
    String strategy() default "";

    /**
     * Other comments about the TCKTest
     */
    String[] notes() default {};
}
