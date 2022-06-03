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
@Named("CDIDependentScopedBatchletContexts")
public class DependentScopedBatchletContexts implements Batchlet {

    //
    // As mentioned in issue: https://github.com/eclipse-ee4j/batch-tck/issues/49
    // the fields getting set via constructor/method parameter injection should not
    // themselves be annotated with "@Inject".
    //
    @Inject JobContext jf; 
    JobContext jc;
    JobContext jm;

    @Inject StepContext sf; 
    StepContext sc;
    StepContext sm;

    @Inject
    DependentScopedBatchletContexts(JobContext jc, StepContext sc) {
        this.jc = jc;
        this.sc = sc;
    }

    @Inject  
    public void setMethod1(JobContext jm) {
        this.jm = jm;
    }
    
    @Inject  
    public void setMethod2(StepContext sm) {
        this.sm = sm;
    }

    @Override
    public String process() throws Exception {

        updateJobExitStatus(jf);
        updateJobExitStatus(jc);
        updateJobExitStatus(jm);
        updateStepExitStatus(sf);
        updateStepExitStatus(sc);
        updateStepExitStatus(sm);

        //
        // Spec doesn't clearly specify precedence between exit status set into StepContext vs. exit status returned by Batchlet process().
        // To avoid ambiguity, ensure the same value is used by both mechanisms.
        //
        // See: https://github.com/eclipse-ee4j/batch-tck/issues/49
        //
        return sm.getExitStatus();
    }

    private void updateJobExitStatus(JobContext jobCtx) {
        String es = jobCtx.getExitStatus();
        es = (es == null ? "" : es);
        StringBuilder sb = new StringBuilder(es);
        sb.append(jobCtx.getExecutionId()).append(":");
        jobCtx.setExitStatus(sb.toString());
    }
    
    private void updateStepExitStatus(StepContext stepCtx) {
        String es = stepCtx.getExitStatus();
        es = (es == null ? "" : es);
        StringBuilder sb = new StringBuilder(es);
        sb.append(stepCtx.getStepExecutionId()).append(":");
        stepCtx.setExitStatus(sb.toString());
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
