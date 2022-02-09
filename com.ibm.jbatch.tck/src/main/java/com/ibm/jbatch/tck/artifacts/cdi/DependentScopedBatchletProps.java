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
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Dependent
@Named("CDIDependentScopedBatchletProps")
public class DependentScopedBatchletProps implements Batchlet {

    @Inject @BatchProperty(name="f1") String field1;
    @Inject @BatchProperty String f2;
    @Inject AppScopedTestBean appScoped;
    @Inject DependentScopedTestBean dependentScoped;
    @Inject JobContext jobCtx; 
    
    private String method1;
    private String method2;

    private String ctor1;
    private String ctor2;

    @Inject
    DependentScopedBatchletProps(@BatchProperty(name="c1") String ctor1, @BatchProperty(name="c2") String c2) {
        this.ctor1 = ctor1;
        this.ctor2 = c2;
    }

    @Inject  
    public void setMethod1(@BatchProperty(name="m1") String method1) {
        this.method1 = method1;
    }
    
    @Inject  
    public void setMethod2(@BatchProperty(name="m2") String m2) {
        this.method2 = m2;
    }
    
    @Override
    public String process() throws Exception {
        if (dependentScoped == null || appScoped == null) {
            throw new Exception("TEST FAILED");
        } else {
            jobCtx.setExitStatus(String.join(":", ctor1, ctor2, field1, f2, method1, method2));
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
