/*
* Copyright 2021 International Business Machines Corp. and others
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
package com.ibm.jbatch.tck.artifacts.cdi;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;

public class NonCDIBeanBatchlet implements Batchlet {

    private class BatchPropertyLiteral extends AnnotationLiteral<BatchProperty> implements BatchProperty {

        private String name;

        BatchPropertyLiteral(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }
    }


    @Override
    public String process() throws Exception {
        CDI<Object> cdi = CDI.current();
        
        JobContext jobCtx = cdi.select(JobContext.class).get();
        StepContext stepCtx = cdi.select(StepContext.class).get();
        String prop1Val =  cdi.select(String.class, new BatchPropertyLiteral("prop1")).get();
        String prop2Val =  cdi.select(String.class, new BatchPropertyLiteral("prop2")).get();


        appendExitStatus(jobCtx, jobCtx.getExecutionId() + ":" + stepCtx.getStepName() + ":" + prop1Val + ":" + prop2Val);

        return "OK";
    }
    


    private void appendExitStatus(JobContext jobCtx, String toAppend) {
        String es = jobCtx.getExitStatus();
        if (es == null) {
            jobCtx.setExitStatus(toAppend);
        } else {
            jobCtx.setExitStatus(es + "," + toAppend);
        }
        
    }

    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        
    }

}
