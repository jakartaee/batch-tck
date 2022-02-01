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
package com.ibm.jbatch.tck.tests.appjoboperatorbean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.jbatch.tck.utils.BaseJUnit5Test;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;
import ee.jakarta.tck.batch.api.Reporter;

import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

public class InjectAppProvidedJobOperatorTests extends BaseJUnit5Test {

    private static JobOperatorBridge jobOp = null;

    /**
     * @testName: testCDIJobOperatorInject
     * @assertion: Section 10.4. JobOperator - TODO (update?)
     * @test_Strategy: First validate the JobOperator function of the injected JbobOperator.  I.e. validate within batch job (batchlet) 
     * that @Inject(ed) JobOperator provides a view of running executions that matches that of the JobContext injected into the batchlet 
     * (i.e. execution id matches). Set as job exit status then validate again in the JUnit logic that the exit status matches the job 
     * execution id obtained via the JobOperator for the just-executed job.
     *                 Second, the step will return the injected JobOperator FQCN as the step exit status.   This variation is intended
     * to validate the fact that the JobOperator provided by the TCK (i.e. as the "application") will take precedence over any JobOperator provided by
     * the batch implementation.  So we confirm the FQCN set in the exit status matches the expected FQCN of the app/TCK-provided JobOperator, (which simply
     * wraps the JobOperator returned by BatchRuntime.getJobOperator().
     *                 Note that in contrast to the similar method in InjectImplProvidedJobOperatorTests, we require that the app/TCK-provided JobOperator bean 
     * appear on the classpath.  In place of a good way to register/deregister the bean dynamically per-test, we require that the two tests run in a separate lifecyle or execution, 
     * so that they can run with distinct classpaths.
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIJobOperatorInjectedBatchlet", "jobOperatorInjectedBatchlet", "com.ibm.jbatch.tck.artifacts.cdi.JobOperatorInjectedBatchlet"})
    public void testCDIJobOperatorInject(String refName) throws Exception {

        String METHOD = "testCDIJobOperatorInject";
        
        String expectedTckAppProvidedJobOperatorClassName = "com.ibm.jbatch.tck.cdi.jobop.TCKJobOperatorWrapper";

        try {
        	Properties jobParams = new Properties();
        	jobParams.setProperty("refName", refName);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_inject_beans", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job didn't complete successfully");
            String exitStatus = jobExec.getExitStatus();
            Reporter.log("job completed with exit status: " + exitStatus);
            String expectedExitStatus = Long.toString(jobExec.getExecutionId());
            assertEquals(expectedExitStatus, jobExec.getExitStatus(), "Test fails - unexpected exit status");

            // Also validate step execution status with TCK-provided JobOperator bean
            List<StepExecution> steps = jobOp.getStepExecutions(jobExec.getExecutionId());
            assertEquals(1, steps.size(), "Wrong number of step executions found");
            assertEquals(expectedTckAppProvidedJobOperatorClassName, steps.get(0).getExitStatus(), "Wrong JobOperator impl class found");

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
