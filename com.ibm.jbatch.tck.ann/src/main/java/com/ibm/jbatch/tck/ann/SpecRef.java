/*
 * Copyright 2015 International Business Machines Corp.
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
 * Reference to a Jakarta Batch section that specifies a particular behavior.
 * <br><br>
 * <span style='font-weight:bold;'> Required Attributes: </span> version, section
 * <br>
 * Optional Attributes: citations, notes
 */
@Target({ElementType.METHOD}) 
@Documented  
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecRef {

	/** 
	 * Version of Jakarta Batch where the behavior was first introduced or most recently clarified
	 * <br><br>
	 * List of valid values:
	 * <ul>
	 *   <li>1.0</li>
	 *   <li>1.0RevA</li>
	 *   <li>1.1</li>
	 * </ul>
	 */
	String version();
	
	/** Jakarta Batch section where the behavior is specified */
	String section() default "";
	
	/** Relevant quotations from the section */
	String[] citations() default{};
	
	/** Other comments about the SpecRef */
	String[] notes() default {};	
}
