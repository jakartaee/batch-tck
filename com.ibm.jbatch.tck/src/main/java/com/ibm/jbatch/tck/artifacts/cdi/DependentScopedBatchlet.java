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

import java.io.StringWriter;
import java.util.Properties;

import com.ibm.jbatch.tck.cdi.AppScopedTestBean;
import com.ibm.jbatch.tck.cdi.DependentScopedTestBean;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Dependent
@Named("CDIDependentScopedBatchlet")
public class DependentScopedBatchlet implements Batchlet {

    @Inject @BatchProperty(name="prop1") String prop1;
    @Inject AppScopedTestBean appScoped;
    @Inject DependentScopedTestBean dependentScoped;
    @Inject JobContext jobCtx; 
    @Inject StepContext stepCtx; 

    private void error(String errorMsg) throws Exception {
        jobCtx.setExitStatus("FAIL: " + errorMsg); throw new Exception(errorMsg);
    }

    @Override
    public String process() throws Exception {
        if (dependentScoped != null && appScoped != null) {
            if (jobCtx.getExecutionId() != dependentScoped.getJobContextExecId()) {
                error("jobCtx execution ids don't match, found: " + dependentScoped.getJobContextExecId());
            } else if (stepCtx.getStepExecutionId() != dependentScoped.getStepContextExecId()) {
                error("step execution ids don't match, found: " + dependentScoped.getStepContextExecId());
            } else if ( prop1 != dependentScoped.getProp1Val()) {
                error("prop1 property values don't match, found: " + dependentScoped.getProp1Val());
            }
            jobCtx.setExitStatus(jobCtx.getExecutionId() + ":" + stepCtx.getStepExecutionId() + ":" + prop1);
        } else {
            error("Null among dependentScoped = " + dependentScoped + ", appScoped = " + appScoped );
        }
        return "OK";
    }


    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public static String getPropertyAsString(Properties prop) throws Exception {
        StringWriter writer = new StringWriter();
        prop.store(writer, "");
        return writer.getBuffer().toString();
    }
}
