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
@Named("CDIDependentScopedBatchletInjectListener")
public class DependentScopedBatchletInjectListener implements Batchlet {

    @Inject ApplicationScopedStepListener listener;
    @Inject JobContext jobCtx;

    @Override
    public String process() throws Exception {
        updateJobExitStatus(Integer.toString(listener.getCount()));
        return "OK";
    }

    private void updateJobExitStatus(String msg) {
        String es = jobCtx.getExitStatus();
        es = (es == null ? "" : es);
        StringBuilder sb = new StringBuilder(es);
        sb.append(msg).append(":");
        jobCtx.setExitStatus(sb.toString());
    }

    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        
    }

}
