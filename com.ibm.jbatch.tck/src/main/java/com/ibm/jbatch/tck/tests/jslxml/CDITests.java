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

public class CDITests extends BaseJUnit5Test {

    private static JobOperatorBridge jobOp = null;

    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: 
     */
//    @Test
//    @Disabled
//    public void testAppScoped() throws Exception {
//
//        String METHOD = "testAppScoped";
//
//        try {
//            Reporter.log("starting job");
//            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_app_scoped", null);
//            Reporter.log("Job Status = " + jobExec.getBatchStatus());
//            assertWithMessage("Job completed", BatchStatus.COMPLETED, jobExec.getBatchStatus());
//            assertWithMessage("Exit status = 1", "1", jobExec.getExitStatus());
//            Reporter.log("job completed");
//        } catch (Exception e) {
//            handleException(METHOD, e);
//        }
//    }
//    
    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: 
     */
    @ParameterizedTest
    @ValueSource(strings = {"CDIDependentScopedBatchlet", "dependentScopedBatchlet", "com.ibm.jbatch.tck.artifacts.cdi.DependentScopedBatchlet"})
    public void testCDI1(String refName) throws Exception {

        String METHOD = "testCDI1";

        try {
        	Properties jobParams = new Properties();
        	jobParams.setProperty("refName", refName);
            Reporter.log("starting job with refName = " + refName);
            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_1", jobParams);
            Reporter.log("Job Status = " + jobExec.getBatchStatus());
            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job completed");
            Reporter.log("job completed");
            assertEquals("GOOD", jobExec.getExitStatus(), "Test succeeded");
            Reporter.log("GOOD result");
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    /**
     * @throws Exception
     * @testName: 
     * @assertion: Section 
     * @test_Strategy: 
     */
//    @Test
//    public void testCDI2() throws Exception {
//
//        String METHOD = "testCDI2";
//
//        try {
//            Reporter.log("starting job");
//            JobExecution jobExec = jobOp.startJobAndWaitForResult("cdi_2", null);
//            Reporter.log("Job Status = " + jobExec.getBatchStatus());
//            assertEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus(), "Job completed");
//            Reporter.log("job completed");
//            String exitStatus = jobExec.getExitStatus();
//            assertEquals("GOOD", jobExec.getExitStatus(), "Test succeeded");
//            Reporter.log("GOOD result");
//        } catch (Exception e) {
//            handleException(METHOD, e);
//        }
//    }

    private Properties loadFromStatus(String status) throws Exception {
    	StringReader sr = new StringReader(status);
    	Properties p = new Properties();
    	p.load(sr);
    	return p;
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
