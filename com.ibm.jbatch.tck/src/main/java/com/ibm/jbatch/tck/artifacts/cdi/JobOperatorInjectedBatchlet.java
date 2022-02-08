/*
* Copyright 2021, 2022 International Business Machines Corp. and others
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

import java.util.List;

import jakarta.batch.api.Batchlet;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Dependent
@Named("CDIJobOperatorInjectedBatchlet")
public class JobOperatorInjectedBatchlet implements Batchlet {

    @Inject JobContext jobCtx; 
    @Inject JobOperator jobOperator;

    private void error(String errorMsg) throws Exception {
        jobCtx.setExitStatus("FAIL: " + errorMsg); throw new Exception(errorMsg);
    }

    @Override
    public String process() throws Exception {
        long jobExecId = jobCtx.getExecutionId();
        String jobName = jobCtx.getJobName();

        List<Long> runningExecs = jobOperator.getRunningExecutions(jobName);
        if (!runningExecs.contains(jobExecId)) {
            error("JobOperator doesn't show: " + jobExecId + " in running executions list, shows: " + runningExecs);
        }
        jobCtx.setExitStatus(Long.toString(jobExecId));
        
        return jobOperator.getClass().getCanonicalName();
    }    


    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        
    }


}
