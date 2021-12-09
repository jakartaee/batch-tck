/*
 * Copyright 2013, 2021 International Business Machines Corp. and others
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
package com.ibm.jbatch.tck.tests.jslxml;

import static com.ibm.jbatch.tck.utils.AssertionUtils.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.jbatch.tck.utils.BaseJUnit5Test;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import com.ibm.jbatch.tck.utils.Reporter;

import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

public class CDITests extends BaseJUnit5Test {

    private static JobOperatorBridge jobOp = null;

    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: validate within batch job (batchlet) that inject bean ctx and property values match the ctx and property values injected into
     *   the batchlet itself.  Then validate again in the JUnit test logic that these injected values match the ones passed to JobOperator and from the job repository.
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIDependentScopedBatchlet", "dependentScopedBatchlet", "com.ibm.jbatch.tck.artifacts.cdi.DependentScopedBatchlet"})
    public void testCDIInject(String refName) throws Exception {

        String METHOD = "testCDIInject";

        try {
        	String parm1Val = "It's a parm";
        	Properties jobParams = new Properties();
        	jobParams.setProperty("refName", refName);
        	jobParams.setProperty("parm1", parm1Val);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_beans", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            // Expecting exit status of: <jobExecId>:<stepExecId>:<parm1Val>
            List<StepExecution> steps = jobOp.getStepExecutions(jobExec.getExecutionId());
            assertEquals(1, steps.size(), "Wrong number of step executions found");
            String expectedExitStatus = jobExec.getExecutionId() + ":" + steps.get(0).getStepExecutionId() + ":" + parm1Val;
            assertEquals(expectedExitStatus, jobExec.getExitStatus(), "Test fails - unexpected exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: validate within batch job (batchlet) that inject bean ctx and property values match the ctx and property values injected into
     *   the batchlet itself.  Then validate again in the JUnit test logic that these injected values match the ones passed to JobOperator and from the job repository.
     *   Since it's ApplicationScoped we want to run it twice to make sure something's not cached incorrectly.
     *   
     *   
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIApplicationScopedBatchlet", "applicationScopedBatchlet", "com.ibm.jbatch.tck.artifacts.cdi.ApplicationScopedBatchlet"})
    public void testCDILazyInject(String refName) throws Exception {

        String METHOD = "testCDILazyInject";

        try {
        	String parm1Val = "It's a parm";
        	String parm2Val = "Or a prop";
        	Properties jobParams = new Properties();
        	jobParams.setProperty("refName", refName);
        	jobParams.setProperty("parm1", parm1Val);
        	jobParams.setProperty("parm2", parm2Val);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_beans_2step", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            List<StepExecution> steps = jobOp.getStepExecutions(jobExec.getExecutionId());
            assertEquals(2, steps.size(), "Wrong number of step executions found");
            /*
             * Expecting exit status of: 
             *   <jobExecId>:step1:<parm1Val>:<parm2Val>,<jobExecId>:step2:s2<parm1Val>:s2<parm2Val>
             */
            String expectedExitStatus = jobExec.getExecutionId() + ":step1:" + parm1Val + ":" + parm2Val + ","
             + jobExec.getExecutionId() + ":step2:" + "s2" + parm1Val + ":" + "s2" + parm2Val;
            assertEquals(expectedExitStatus, jobExec.getExitStatus(), "Test fails - unexpected exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    
    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: validate within batch job (batchlet) that inject bean ctx and property values match the ctx and property values injected into
     *   the batchlet itself.  Then validate again in the JUnit test logic that these injected values match the ones passed to JobOperator and from the job repository.
     *   Since it's ApplicationScoped we want to run it twice to make sure something's not cached incorrectly.
     *   
     *   
     */
    @ParameterizedTest
    @ValueSource(strings="com.ibm.jbatch.tck.artifacts.cdi.NonCDIBeanBatchlet")
    public void testCDILookup(String refName) throws Exception {

        String METHOD = "testCDILookup";

        try {
        	String parm1Val = "It's a parm";
        	String parm2Val = "Or a prop";
        	Properties jobParams = new Properties();
        	jobParams.setProperty("refName", refName);
        	jobParams.setProperty("parm1", parm1Val);
        	jobParams.setProperty("parm2", parm2Val);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_beans_2step", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            List<StepExecution> steps = jobOp.getStepExecutions(jobExec.getExecutionId());
            assertEquals(2, steps.size(), "Wrong number of step executions found");
            /*
             * Expecting exit status of: 
             *   <jobExecId>:step1:<parm1Val>:<parm2Val>,<jobExecId>:step2:s2<parm1Val>:s2<parm2Val>
             */
            String expectedExitStatus = jobExec.getExecutionId() + ":step1:" + parm1Val + ":" + parm2Val + ","
             + jobExec.getExecutionId() + ":step2:" + "s2" + parm1Val + ":" + "s2" + parm2Val;
            assertEquals(expectedExitStatus, jobExec.getExitStatus(), "Test fails - unexpected exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: validate within test based on status set within job 
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIDependentScopedBatchletProps", "dependentScopedBatchletProps", "com.ibm.jbatch.tck.artifacts.cdi.DependentScopedBatchletProps"})
    public void testCDIBatchProps(String refName) throws Exception {

        String METHOD = "testCDIBatchProps";

        try {
        	Properties jobParams = new Properties();
        	String ctor1 = "CTOR";
        	String field1 = "ABC";
        	String method1 = "XYZ";
        	jobParams.setProperty("refName", refName);
        	jobParams.setProperty("ctor1", ctor1);
        	jobParams.setProperty("field1", field1);
        	jobParams.setProperty("method1", method1);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_batch_props", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            Reporter.log("job completed with exit status = " + jobExec.getExitStatus());
            // ES => <c1>:<field1>:<method1>
            assertEquals(ctor1 + ":" + field1 + ":" + method1, jobExec.getExitStatus(), "Test fails - unexpected exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }


    private static void handleException(String methodName, Exception e) throws Exception {
        Reporter.log("Caught exception: " + e.getMessage() + "<p>");
        Reporter.log(methodName + " failed<p>");
        throw e;
    }


    @BeforeAll
    public static void beforeTest() throws ClassNotFoundException {
        jobOp = new JobOperatorBridge();
    }

    @AfterAll
    public static void afterTest() {
        jobOp = null;
    }
}
