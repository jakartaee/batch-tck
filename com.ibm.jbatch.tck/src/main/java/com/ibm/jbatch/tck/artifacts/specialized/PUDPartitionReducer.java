/**
 * Copyright 2016 International Business Machines Corp.
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
package com.ibm.jbatch.tck.artifacts.specialized;

import javax.batch.api.partition.AbstractPartitionReducer;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

@javax.inject.Named("PUDPartitionReducer")
public class PUDPartitionReducer extends AbstractPartitionReducer {
    
    @Inject
    StepContext stepCtx;
	
    public final String TOP_LEVEL_PUD = "This is the Persistent User Data for the top-level stepCtx of the partitioned step!";
    
    @Override
    public void beginPartitionedStep() throws Exception {
	    stepCtx.setPersistentUserData(TOP_LEVEL_PUD);
    }
    
    @Override
    public void afterPartitionedStepCompletion(PartitionStatus status) throws Exception {
        if (!stepCtx.getPersistentUserData().equals(TOP_LEVEL_PUD)) {
            throw new Exception("Unexpected PUD at the top level of the Partitioned Step!");
        }
    }

}
