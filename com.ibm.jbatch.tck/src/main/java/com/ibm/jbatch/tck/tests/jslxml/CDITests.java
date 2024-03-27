/*
 * Copyright 2021, 2024 International Business Machines Corp. and others
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
import ee.jakarta.tck.batch.util.Reporter;

import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

public class CDITests extends BaseJUnit5Test {

    private static JobOperatorBridge jobOp = null;

    /**
     * @testName: testCDIInject
     * @assertion: Section 9.3.5.2. Batch Property Values Resolved Based on "current batch artifact" on Thread;   
     *             Section 9.4.3 CDI-Related Context Requirements
     * @test_Strategy: Validate within batch job (batchlet) that a CDI Bean injected into the batchlet has a JobContext and batch property value injected into this Bean 
     * with property and context id values of the injected bean matching the property and context id values of the batchlet itself.  This proves that a non-batch-artifact
     * Bean can get injected with batch properties and context from the thread of execution.
     * Then validate again in the JUnit test logic that these injected values match the ones obtained via JobOperator from the job repository.
     * 
     * Parameterize test with three artifact loading syntaxes:  bean name, batch.xml lookup, and FQCN
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
     * @testName: testCDIInjectContexts
     * @assertion: Section 9.4.3 CDI-Related Context Requirements 
     * @test_Strategy: Within batch job (batchlet) inject batch contexts using a variety of injection styles: field, method and constructor parms, 
     * multiple parameter methods, etc, and use the execution ids built from these injected contexts to set a job exit status. Then validate in the 
     * JUnit test logic that this exit status matches the expected value, based on the ids obtained from the JobOperator from the just-executed job.
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIDependentScopedBatchletContexts", "dependentScopedBatchletContexts", "com.ibm.jbatch.tck.artifacts.cdi.DependentScopedBatchletContexts"})
    public void testCDIInjectContexts(String refName) throws Exception {

        String METHOD = "testCDIInjectContexts";

        try {
            Properties jobParams = new Properties();
            jobParams.setProperty("refName", refName);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_beans", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            // Expecting exit status of: <jobExecId>:<stepExecId>:<parm1Val>
            List<StepExecution> steps = jobOp.getStepExecutions(jobExec.getExecutionId());
            assertEquals(1, steps.size(), "Wrong number of step executions found");
            Reporter.log("step completed with exit status: " + steps.get(0).getExitStatus());
            long stepExecId = steps.get(0).getStepExecutionId();
            String expectedJobExitStatus = jobExec.getExecutionId() + ":" + jobExec.getExecutionId() + ":" + jobExec.getExecutionId() + ":";
            assertEquals(expectedJobExitStatus, jobExec.getExitStatus(), "Test fails - unexpected job exit status");
            String expectedStepExitStatus = stepExecId + ":" + stepExecId + ":" + stepExecId + ":";
            assertEquals(expectedStepExitStatus, steps.get(0).getExitStatus(), "Test fails - unexpected step exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /**
     * @testName: testCDIInjectListenerIntoBatchlet 
     * @assertion: Section 10.5.1. Required Artifact Loading Sequence
     * @test_Strategy: Inject the same @ApplicationScoped listener into a batchlet used in each step of a three step job.
     * Configure the listener as a step listener in two of the three steps.  Read a count that gets incremeneted in the listener
     * instance from each batchlet, to confirm it is a single instance of the @ApplicationScoped listener used across all these usages.
     * 
     * Don't parameterize tests since the single app scoped bean would have different state among each, complicating test logic.
     */
    @Test
    public void testCDIInjectListenerIntoBatchlet() throws Exception {

        String METHOD = "testCDIInjectListenerIntoBatchlet";

        try {
            Properties jobParams = new Properties();
            jobParams.setProperty("listenerName", "CDIApplicationScopedStepListener");
            jobParams.setProperty("batchletName", "CDIDependentScopedBatchletInjectListener");
            Reporter.log("starting job for testCDIInjectListenerIntoBatchlet");
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_listener_into_batchlet", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            String expectedJobExitStatus = "2:4:5:";
            assertEquals(expectedJobExitStatus, jobExec.getExitStatus(), "Test fails - unexpected job exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    
    /**
     * @testName: testCDIBatchProps
     * @assertion: Section 9.3.5. CDI-Related Batch Property Requirements
     * @test_Strategy: Within batch job (batchlet) inject two batch properties using a variety of injection styles: fields (explicitly named and defaulted), 
     * method and constructor parms, multiple parameter methods, etc.  In the batchlet set a job exit status, then validate in the JUnit test logic that these injected 
     * values in the exist status match the expected value.
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIDependentScopedBatchletProps", "dependentScopedBatchletProps", "com.ibm.jbatch.tck.artifacts.cdi.DependentScopedBatchletProps"})
    public void testCDIBatchProps(String refName) throws Exception {

        String METHOD = "testCDIBatchProps";

        try {
            Properties jobParams = new Properties();
            String ctor1 = "CTOR";
            String ctor2 = "CAT";
            String field1 = "ABC";
            String field2 = "APPLE";
            String method1 = "XYZ";
            String method2 = "X-WING";
            jobParams.setProperty("refName", refName);
            jobParams.setProperty("c1", ctor1);
            jobParams.setProperty("c2", ctor2);
            jobParams.setProperty("f1", field1);
            jobParams.setProperty("f2", field2);
            jobParams.setProperty("m1", method1);
            jobParams.setProperty("m2", method2);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_batch_props", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            Reporter.log("job completed with exit status = " + jobExec.getExitStatus());
            String expectedExitStatus = String.join(":", ctor1, ctor2, field1, field2, method1, method2);
            assertEquals(expectedExitStatus, jobExec.getExitStatus(), "Test fails - unexpected exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /**
     * @testName: testCDIInjectRepeatProps
     * @assertion: Section 9.3.5. CDI-Related Batch Property Requirements
     * @test_Strategy: Within batch job (batchlet) inject two batch properties using a variety of injection styles: fields (explicitly named and defaulted), 
     * method and constructor parms, multiple parameter methods, etc.  In the batchlet set a job exit status, then validate in the JUnit test logic that these injected 
     * values in the exist status match the expected value.
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIDependentScopedBatchletRepeatProps", "dependentScopedBatchletRepeatProps", "com.ibm.jbatch.tck.artifacts.cdi.DependentScopedBatchletRepeatProps"})
    public void testCDIInjectRepeatProps(String refName) throws Exception {

        String METHOD = "testCDIInjectRepeatProps";

        try {
            String p1 = "myParm1";
            String p2 = "myParm2";
            Properties jobParams = new Properties();
            jobParams.setProperty("refName", refName);
            jobParams.setProperty("parm1", p1);
            jobParams.setProperty("parm2", p2);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_beans", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            // Expecting exit status of: <jobExecId>:<stepExecId>:<parm1Val>
            String expectedExitStatus = String.join(":", p1, p2, p1, p1, p2, p1, p1, p2, p1, p1, p1, p1);
            assertEquals(expectedExitStatus, jobExec.getExitStatus(), "Test fails - unexpected exit status");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /**
     * @testName: testCDILazyInject 
     * @assertion: Section 9.3.5.2. Batch Property Values Resolved Based on "current batch artifact" on Thread
     * @test_Strategy: In an @ApplicationScoped batchlet obtain batch properties and contexts "lazily" by injecting CDI Instance(s) but then only doing
     * Instance.get() during job execution, from a batch execution thread, so the correct values can be injected via CDI and the batch runtime. 
     * Build a job exit status from the batchlet's view of the lazily-obtained property and context values.   Then, from the JUnit logic, validate the
     * exit status against an  the expected exit status built from parameters passed to the job plus execution ids obtained via the JobOperator for the just-executed job. 
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
     * @throws 
     * @testName: testCDILookup
     * @assertion: Section 9.3.5.2. Batch Property Values Resolved Based on "current batch artifact" on Thread 
     * @test_Strategy: In a non-Bean (batch-managed) batchlet artifact,  obtain batch properties and contexts dynamically by "lookup" by 
     * using CDI#select() from the non-Bean batchlet during job execution, from a batch execution thread, so the correct values can be injected via CDI and the batch runtime. 
     * Build a job exit status from the batchlet's view of the lazily-obtained property and context values.   Then, from the JUnit logic, validate the
     * exit status against an  the expected exit status built from parameters passed to the job plus execution ids obtained via the JobOperator for the just-executed job. 
     */
    @ParameterizedTest
    @ValueSource(strings="com.ibm.jbatch.tck.artifacts.cdi.NonCDIBeanBatchlet")
    @Disabled("https://github.com/jakartaee/batch-tck/issues/71")
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
