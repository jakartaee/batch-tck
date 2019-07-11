/*
 * Copyright 2016 International Business Machines Corp.
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

import static com.ibm.jbatch.tck.utils.AssertionUtils.assertObjEquals;

import java.util.Properties;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

import com.ibm.jbatch.tck.ann.*;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExecutionTests {

	private static JobOperatorBridge jobOp;

	public static void setup(String[] args, Properties props) throws Exception {
		String METHOD = "setup";

		try {
			jobOp = new JobOperatorBridge();  
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	@BeforeMethod
	@BeforeClass
	public static void setUp() throws Exception {
		jobOp = new JobOperatorBridge();
	}

	@AfterClass
	public static void cleanup() throws Exception {
	}

	private void begin(String str) {
		Reporter.log("Begin test method: " + str);
	}

	/*
	 * @testName: testInvokeJobWithOneBatchletStep
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test  
	public void testInvokeJobWithOneBatchletStep() throws Exception {
		String METHOD = "testInvokeJobWithOneBatchletStep";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_1step.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_1step");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			assertObjEquals("STEP 1 COMPLETED", jobExec.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobWithTwoStepSequenceOfBatchlets
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test
	public void testInvokeJobWithTwoStepSequenceOfBatchlets() throws Exception {
		String METHOD = "testInvokeJobWithTwoStepSequenceOfBatchlets";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_2steps.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_2steps");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			assertObjEquals("STEP 2 COMPLETED", jobExec.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobWithFourStepSequenceOfBatchlets
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test
	public void testInvokeJobWithFourStepSequenceOfBatchlets() throws Exception {
		String METHOD = "testInvokeJobWithFourStepSequenceOfBatchlets";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_4steps.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_4steps");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			assertObjEquals("STEP 3 COMPLETED", jobExec.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobWithNextElement
	 * @assertion: job will finish successfully as COMPLETED
	 * @test_Strategy: The job is written with a Next Element to control a step transition. The status of the second step is checked
	 * 		for completion, to prove that the step was properly called from the Next Element.
	 */
	@Test
	@org.junit.Test  
	public void testInvokeJobWithNextElement() throws Exception {
		String METHOD = "testInvokeJobWithNextElement";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_nextElement.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_nextElement");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			assertObjEquals("STEP 2 COMPLETED", jobExec.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}
	
	
   	/*
   	 * @testName: testJobWithNoMatchingTransitionElement
   	 * @assertion: job will finish successfully with an exit status set to "nullUnusedExitStatusForPartitions"
   	 * @test_Strategy: The job is written with no transitions elements in the first step, and no next attribute for step1.
   	 * 					With no clear next step, the job should finish on step 1. The test ensures that step 2 is not run.
   	 */   	
   	@TCKTest(
   		versions="1.1.WORKING",
   		assertions={"Step 2 does not execute"},
   		specRefs={
   			@SpecRef(
   				version="1.0", section="8.9.2", 
   				citations={
   					"If a match is not found among the transition elements [...] If execution ended normally, and the execution element whose execution "
   					+ "is completing does not contain a ‘next’ attribute, then the job ends normally (with COMPLETED batch status)."
   				}
   			)
   		}
   	)
   	@Test
   	@org.junit.Test  
   	public void testJobWithNoMatchingTransitionElement() throws Exception {
   		String METHOD = "testJobWithNoMatchingTransitionElement";
   		begin(METHOD);
   
   		try {
   			Reporter.log("Locate job XML file: job_batchlet_no_matching_element.xml<p>");
   
   			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
   			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_no_matching_element");
   
   			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
   			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
   			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
   			final String expectedExitStatus="nullUnusedExitStatusForPartitions";
   			assertObjEquals(expectedExitStatus, jobExec.getExitStatus());
   		} catch (Exception e) {
   			handleException(METHOD, e);
   		}
   	}

	/*
	 * @testName: testInvokeJobWithFailElement
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test  
	public void testInvokeJobWithFailElement() throws Exception {
		String METHOD = "testInvokeJobWithFailElement";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_failElement.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_failElement");

			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("TEST_FAIL", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobWithStopElement
	 * @assertion: job will finish successfully as STOPPED after the first step
	 * @test_Strategy: A Stop Element is used to terminate the job after the first step. If the job continues, a different status is returned,
	 * 	and the test fails.
	 */
	@Test
	@org.junit.Test  
	public void testInvokeJobWithStopElement() throws Exception {
		String METHOD = "testInvokeJobWithStopElement";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_stopElement.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_stopElement");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			assertObjEquals("TEST_STOPPED", jobExec.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobWithEndElement
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test  
	public void testInvokeJobWithEndElement() throws Exception {
		String METHOD = "testInvokeJobWithEndElement";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_batchlet_endElement.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_batchlet_endElement");

			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("TEST_ENDED", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobSimpleChunk
	 * @assertion: job will finish successfully as COMPLETED
	 * @test_Strategy: A small chunk is run with a reader, processor, and writer. If all items are read, processed, and written
	 * 	without failure, then the exit status will be good and the test will pass.
	 */
	@Test
	@org.junit.Test
	public void testInvokeJobSimpleChunk() throws Exception {
		String METHOD = "testInvokeJobSimpleChunk";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_chunk_simple.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_chunk_simple");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
			Reporter.log("execution #1 JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			assertObjEquals("STEP 2 COMPLETED", jobExec.getExitStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobChunkWithFullAttributes
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@org.junit.Test
	@Test(enabled=false) // Disabling per Bug 5379
	@Ignore("Bug 5379.  Decided to exclude this test.")
	public void testInvokeJobChunkWithFullAttributes() throws Exception {
		String METHOD = "testInvokeJobChunkWithFullAttributes";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_chunk_full_attributes.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_chunk_full_attributes");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testInvokeJobUsingTCCL
	 * @assertion: Section 10.5 Batch Artifact loading
	 * @test_Strategy: Implementation should attempt to load artifact using Thread Context Class Loader if implementation specific
	 * 	and archive loading are unable to find the specified artifact.
	 */
	@Test
	@org.junit.Test  
	public void testInvokeJobUsingTCCL() throws Exception {
		String METHOD = "testInvokeJobUsingTCCL";
		begin(METHOD);

		try {
			Reporter.log("Run job using job XML file: test_artifact_load_classloader<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("test_artifact_load_classloader");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testCheckpoint
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test
	public void testCheckpoint() throws Exception {
		String METHOD = "testCheckpoint";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_chunk_checkpoint.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_chunk_checkpoint");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testSimpleFlow
	 * @assertion: FIXME
	 * @test_Strategy: FIXME
	 */
	@Test
	@org.junit.Test
	public void testSimpleFlow() throws Exception {
		String METHOD = "testSimpleFlow";
		begin(METHOD);

		try {
			Reporter.log("Locate job XML file: job_flow_batchlet_4steps.xml<p>");

			Reporter.log("Invoking startJobAndWaitForResult for Execution #1<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_flow_batchlet_4steps");

			Reporter.log("execution #1 JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch (Exception e) {
			handleException(METHOD, e);
		}

	}


	private static void handleException(String methodName, Exception e) throws Exception {
		Reporter.log("Caught exception: " + e.getMessage()+"<p>");
		Reporter.log(methodName + " failed<p>");
		throw e;
	}

}
