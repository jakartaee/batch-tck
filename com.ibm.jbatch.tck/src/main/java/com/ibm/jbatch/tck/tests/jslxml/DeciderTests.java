/*
 * Copyright 2012 International Business Machines Corp.
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
import static com.ibm.jbatch.tck.utils.AssertionUtils.assertWithMessage;

import java.util.Properties;
import java.util.logging.Logger;

import javax.batch.operations.JobStartException;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

import org.junit.BeforeClass;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ibm.jbatch.tck.ann.*;
import com.ibm.jbatch.tck.artifacts.common.StatusConstants;
import com.ibm.jbatch.tck.artifacts.specialized.DeciderTestsBatchlet;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;

public class DeciderTests implements StatusConstants {
	private final static Logger logger = Logger.getLogger(DeciderTests.class.getName());
	private static JobOperatorBridge jobOp = null;

	private final static String FORCE_STOP_EXITSTATUS = "STEP_COMPLETE_BUT_FORCE_JOB_STOPPED_STATUS";
	private final static String FORCE_FAIL_EXITSTATUS = "STEP_COMPLETE_BUT_FORCE_JOB_FAILED_STATUS";


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

	/* cleanup */
	public void  cleanup()
	{		

	}

	/*
	 * @testName: testDeciderEndNormal
	 * @assertion: Tests that decision child elements: next, step, end, fail, all work as expected (by verifying batch status)
	 *             Tests exit status globbing ('*' and '?') 
	 *             Tests StepExecution from previous step passed to Decider
	 *             Tests that decider can be configured via property
	 *             Tests that JobContext can be utilized in decider (the same instance as that of the earlier steps).
	 *             Tests setting of <stop>, <end>, <fail> @exit-status attribute value.
	 *             
	 * @test_Strategy: The test methods in this class have a tighter coupling than usual.  That's because they all use the
	 *                 same artifacts, the same basic JSL, and the same basic <decision> element (with the same set of glob 
	 *                 patterns). We set up different "branches" in the job execution sequence depending on exit status, 
	 *                 (e.g. stop vs. fail, end with one exit status vs. another), and we take the various branches in the
	 *                 various test methods.
	 *                    
	 *                 1. Batchlet is coded to end with one of two different exit status values depending on @BatchProperty populated by job param set either to:
	 *                       jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);
	 *                              OR
	 *                       jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);
	 *                    Batch status is tested as well
	 *                 2. Decider returns value based on configured property, step execution exit status, and job context transient data. 
	 *                 3. JobContext utilized to adjust "core exit status" by prefixing the number of times the step has run.
	 *                 4. Special globbing chars '*' and '?' are used in the @on values to test exit status globbing.
	 *                 5. Test asserts specific exit status as well as batch status.  In addition to testing the overall test flow this 
	 *                    tests the @exit-status attributes of fail, stop, end.
	 *                    
	 */
	@Test
	@org.junit.Test
	public void testDeciderEndNormal() throws Exception {

		String METHOD = "testDeciderEndNormal";

		try {
			// 1. Here "EndSpecial" is the exit status the decider will return if the step exit status
			// is the "special" exit status value.  It is set as a property on the decider.
			Reporter.log("Build job parameters for EndSpecial exit status<p>");

			Properties jobParameters = new Properties();

			// 2. Here "EndNormal" is the exit status the decider will return if the step exit status
			// is the "normal" exit status value.  It is set as a property on the batchlet and passed
			// along to the decider via stepContext.setTransientUserData().
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value EndNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "EndNormal");
			// 3. This "ACTUAL_VALUE" is a property set on the batchlet.  It will either indicate to end
			// the step with a "normal" or "special" exit status.            
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE + "<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value EndSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "EndSpecial");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			Reporter.log("Expected JobExecution getExitStatus()=EndNormal<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getStatus()=COMPLETED<p>");
			Reporter.log("Actual JobExecution getStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("EndNormal", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}

	}

	/*
	 * @testName: testDeciderEndSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderEndSpecial() throws Exception {
		String METHOD = "testDeciderEndSpecial";

		try {
			Reporter.log("Build job parameters for EndSpecial exit status<p>");	    

			Properties jobParameters = new Properties();        
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "EndNormal");
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value EndNormal<p>");
			// 1. This is the only test parameter that differs from testDeciderEndNormal().
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value EndSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "EndSpecial");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			// 2. And the job exit status differs accordingly.
			Reporter.log("Expected JobExecution getExitStatus()=EndSpecial<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=COMPLETED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("EndSpecial", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderStopNormal
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderStopNormal() throws Exception {
		String METHOD = " testDeciderStopNormal";

		try {
			Reporter.log("Build job parameters for StopSpecial exit status<p>");

			Properties jobParameters = new Properties();    
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value StopNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "StopNormal");

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value StopSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "StopSpecial");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			Reporter.log("Expected JobExecution getExitStatus()=StopNormal<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=STOPPED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("StopNormal", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}

	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderStopSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderStopSpecial() throws Exception {
		String METHOD = "testDeciderStopSpecial";

		try {
			Reporter.log("Build job parameters for StopSpecial exit status<p>");

			Properties jobParameters = new Properties();     
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value StopNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "StopNormal");

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value StopSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "StopSpecial");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			Reporter.log("Expected JobExecution getExitStatus()=StopSpecial<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=STOPPED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("StopSpecial", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderFailNormal
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderFailNormal() throws Exception {

		String METHOD = "testDeciderFailNormal";

		try {
			Reporter.log("Build job parameters for FailSpecial exit status<p>");	    	

			Properties jobParameters = new Properties();        
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value FailNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "FailNormal");

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value FailSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "FailSpecial");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			Reporter.log("Expected JobExecution getExitStatus()=FailNormal<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=FAILED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("FailNormal", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	// See the first two test methods for an explanation of parameter values.
	/*
	 * @testName: testDeciderFailSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderFailSpecial() throws Exception {
		String METHOD = "testDeciderFailSpecial";

		try {
			Reporter.log("Build job parameters for FailSpecial exit status<p>");

			Properties jobParameters = new Properties();        
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value FailNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "FailNormal");

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value FailSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "FailSpecial");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_incompleterun", jobParameters); 

			Reporter.log("Expected JobExecution getExitStatus()=FailSpecial<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=FAILED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals("FailSpecial", jobExec.getExitStatus());
			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderNextNormal
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderNextNormal() throws Exception {
		String METHOD = "testDeciderNextNormal";

		try {
			Reporter.log("Build job parameters for NextSpecial exit status<p>");

			Properties jobParameters = new Properties();        
			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value NextNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "NextNormal");

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.NORMAL_VALUE +"<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.NORMAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value NextSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "NextSpecial");

			Reporter.log("Create single job listener deciderTestJobListener and get JSL<p>");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_completerun", jobParameters); 

			Reporter.log("Expected JobExecution getExitStatus()="+GOOD_JOB_EXIT_STATUS+"<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=COMPLETED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(GOOD_JOB_EXIT_STATUS, jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderNextSpecial
	 * @assertion: see testDeciderEndNormal
	 * @test_Strategy: see testDeciderEndNormal
	 */
	@Test
	@org.junit.Test
	public void testDeciderNextSpecial() throws Exception {
		String METHOD = "testDeciderNextSpecial";

		try {
			Reporter.log("Build job parameters for NextSpecial exit status<p>");	

			Properties jobParameters = new Properties();        

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTION + " with value NextNormal<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTION, "NextNormal");

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.ACTUAL_VALUE + " with value " + DeciderTestsBatchlet.SPECIAL_VALUE + "<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.ACTUAL_VALUE, DeciderTestsBatchlet.SPECIAL_VALUE);

			Reporter.log("Set job parameters property " + DeciderTestsBatchlet.SPECIAL_EXIT_STATUS + " with value NextSpecial<p>");
			jobParameters.setProperty(DeciderTestsBatchlet.SPECIAL_EXIT_STATUS, "NextSpecial");

			Reporter.log("Create single job listener deciderTestJobListener and get JSL<p>");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("job_decider_completerun", jobParameters); 

			// This actually exits with the exact same status as the "...NextNormal" test.
			Reporter.log("Expected JobExecution getExitStatus()="+GOOD_JOB_EXIT_STATUS+"<p>");
			Reporter.log("Actual JobExecution getExitStatus()="+jobExec.getExitStatus()+"<p>");
			Reporter.log("Expected JobExecution getBatchStatus()=COMPLETED<p>");
			Reporter.log("Actual JobExecution getBatchStatus()="+jobExec.getBatchStatus()+"<p>");
			assertObjEquals(GOOD_JOB_EXIT_STATUS, jobExec.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderExitStatusIsSetOnJobContext
	 * @assertion:  Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 * @test_Strategy:  The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *   be equal to the return value of the last decision (i.e. Decider#decide). Note the test doesn't necessarily 
	 *   confirm that the exit status is set on the JobContext directly, but this is the intent behind the test method name.
	 */
	@Test
	@org.junit.Test
	public void testDeciderExitStatusIsSetOnJobContext() throws Exception {

		String METHOD = "testDeciderExitStatusIsSetOnJobContext";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "None");
			jobParameters.setProperty("fail.job.after.this.step", "step1");


			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.FAILED, jobExec.getBatchStatus());
			assertObjEquals("1:"+FORCE_FAIL_EXITSTATUS, jobExec.getExitStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderCannotbeFirstElementOnStart
	 * @assertion: Tests that the first execution element of a job cannot be a decision 
	 *  (it's a misnomer to say 'decider' but we'll leave the test name alone).
	 * @test_Strategy: Since an implementation may either throw a JobStartException or 
	 *  begin an execution which fails, we will pass the test if we see either JobStartException
	 *  or BatchStatus of FAILED.
	 */
	@Test
	@org.junit.Test
	public void testDeciderCannotbeFirstElementOnStart() throws Exception {

		String METHOD = "testDeciderCannotbeFirstElementOnStart";

		try {
			boolean seenException = false;
			JobExecution jobExec = null;
			try {
				jobExec = jobOp.startJobAndWaitForResult("decider_as_first_job_element_fails"); 
			} catch (JobStartException e) {
				Reporter.log("Caught JobStartException:  " + e.getLocalizedMessage());
				seenException = true;
			}

			// If we caught an exception we'd expect that a JobExecution would not have been created,
			// though we won't validate that it wasn't created.  

			// If we didn't catch an exception that we require that the implementation fail the job execution.
			if (!seenException) {
				Reporter.log("Didn't catch JobStartException, Job Batch Status = " + jobExec.getBatchStatus());
				assertWithMessage("Job should have failed because of decision as first execution element.", BatchStatus.FAILED, jobExec.getBatchStatus());
			}
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromStepAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromStepAndAllowRestart() throws Exception {
		String METHOD = "testDeciderTransitionFromStepAndAllowRestart";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");


			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("1:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}


	}

	/*
	 * @testName: testDeciderTransitionFromStepWithinFlowAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that a decision within a flow can terminate the top-level job through appropriate transition elements
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *                  3. A decision within a flow is configured to stop (based on exit status matching against a <stop> element).
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromStepWithinFlowAndAllowRestart() throws Exception {

		String METHOD = "testDeciderTransitionFromStepWithinFlowAndAllowRestart";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("2:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromFlowAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a flow is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution of the last step in the preceding flow.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromFlowAndAllowRestart() throws Exception {

		String METHOD = "testDeciderTransitionFromFlowAndAllowRestart";


		try {
			Reporter.log("Build job parameters.<p>");    
			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step2");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("3:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}



	/*
	 * @testName: testDeciderTransitionFromSplitAndAllowRestart
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed StepExecution(s) 
	 *                 corresponding to the new, restarted execution (not the original execution). 
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a split is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution(s) of each of the last steps of the member flows in the preceding split.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Steps are configured with allow-start-if-complete = true.  Job parameter is used to vary 
	 *                     exit status on original vs. restart execution and, on restart, the
	 *                     StepExecution exit status is asserted to be the one on the restarted execution. 
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *              	3. NOTE: TODO for future - Perhaps the strategy in asserting that EACH StepExecution is passed should be
	 *                     tightened.  We could go further to assert that the full list of StepExecution(s) is what we'd expect.
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromSplitAndAllowRestart() throws Exception {

		String METHOD = "testDeciderTransitionFromSplitAndAllowRestart";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "true");

			jobParameters.setProperty("stop.job.after.this.step", "split1flow1step2");
			jobParameters.setProperty("stop.job.after.this.step2", "split1flow2step2");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_from_split_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("4:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			jobParameters.setProperty("stop.job.after.this.step2", "None");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("4:split1flow2step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromStepAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                  test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                  from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                  execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                  to return a different result on restart.
	 *                  JobContext transient user data is used to assert the correct number of decider invocations have
	 *                  been performed.
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromStepAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromStepAndAllowRestartFalse";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");


			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("1:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromStepWithinFlowAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that a decision within a flow can terminate the top-level job through appropriate transition elements
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                     test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                     from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                     execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                     to return a different result on restart.      
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *                  3. A decision within a flow is configured to stop (based on exit status matching against a <stop> element).
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromStepWithinFlowAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromStepWithinFlowAndAllowRestartFalse";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step1");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("2:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}

	/*
	 * @testName: testDeciderTransitionFromFlowAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a flow is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution of the last step in the preceding flow.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                     test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                     from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                     execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                     to return a different result on restart.
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromFlowAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromFlowAndAllowRestartFalse";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "flow1step2");
			jobParameters.setProperty("fail.job.after.this.step", "None");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("3:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("3:flow1step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}


	/*
	 * @testName: testDeciderTransitionFromSplitAndAllowRestartFalse
	 * @assertion:  1. Tests that the exit return value of Decider#decide is set as the value of the job's exit status.
	 *              2. Tests that the decider re-executes on a restart and the decide() method is passed the 
	 *                 StepExecution corresponding to the original execution (since the step doesn't re-execute on the
	 *                 new, restart execution),  with the original StepExecution's exit status.
	 *                 (See Sec. 10.8 Restart Processing)
	 *              3. Tests that when a split is followed by a decision, that the decision element's Decider#decide 
	 *                 will be passed the StepExecution(s) of each of the last steps of the member flows in the preceding split.
	 * @test_Strategy:  1. The exit status is not set via JobContext#setExitStatus or other means, but is asserted to 
	 *                     be equal to the return value of the last decision (i.e. Decider#decide).
	 *                  2. Allow-start-if-complete for the steps is set to "false".   (This is where the "False" in the
	 *                     test method name comes from).   Decision is configured to expect the StepExecution exitStatus
	 *                     from the original execution, and the test will fail if not.  I.e. the exit status from the earlier 
	 *                     execution is confirmed to have been persisted. Job parameter is used to allow Decider#decide
	 *                     to return a different result on restart.
	 *                     JobContext transient user data is used to assert the correct number of decider invocations have
	 *                     been performed.
	 *              	3. NOTE: TODO for future - Perhaps the strategy in asserting that EACH StepExecution is passed should be
	 *                     tightened.  We could go further to assert that the full list of StepExecution(s) is what we'd expect.
	 */
	@Test
	@org.junit.Test
	public void testDeciderTransitionFromSplitAndAllowRestartFalse() throws Exception {

		String METHOD = "testDeciderTransitionFromSplitAndAllowRestartFalse";

		try {
			Reporter.log("Build job parameters.<p>");    

			Properties jobParameters = new Properties();
			jobParameters.setProperty("step.complete.but.force.job.stopped.status", FORCE_STOP_EXITSTATUS);
			jobParameters.setProperty("step.complete.but.force.job.failed.status", FORCE_FAIL_EXITSTATUS);

			jobParameters.setProperty("allow.start.if.complete", "false");

			jobParameters.setProperty("stop.job.after.this.step", "split1flow1step2");
			jobParameters.setProperty("stop.job.after.this.step2", "split1flow2step2");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec = jobOp.startJobAndWaitForResult("decider_transitions_from_split_on_restart", jobParameters); 

			assertObjEquals(BatchStatus.STOPPED, jobExec.getBatchStatus());
			assertObjEquals("4:"+FORCE_STOP_EXITSTATUS, jobExec.getExitStatus());

			Properties restartJobParameters = new Properties(jobParameters);
			restartJobParameters.setProperty("stop.job.after.this.step", "None");
			restartJobParameters.setProperty("is.restart", "true");

			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId(), restartJobParameters);

			assertObjEquals("4:split1flow2step2_CONTINUE", jobExec2.getExitStatus());
			assertObjEquals(BatchStatus.COMPLETED, jobExec2.getBatchStatus());
		} catch(Exception e) {
			handleException(METHOD, e);
		}
	}
	
	@TCKTest(
		versions = {"1.1.WORKING"},
		assertions = {"A Decider that follows a single step is passed the most recent StepExecution of the step."},
		specRefs = {
			@SpecRef(
				version = "1.0", section = "9.6",
				citations = "The decide method sets a new exit status for a job. It receives an array of StepExecution objects as input. These "
						  + "StepExecution objects represent the execution element that transitions to this decider as follows: [...] Step [...] "
						  + "When the transition is from a step, the decide method receives the StepExecution corresponding to the step as input.",
				notes = "API for Decider"
			),
			@SpecRef(
				version = "1.0RevA", section = "10.8.4",
				citations = "The Decider's \"decide\" method is passed a StepExecution array as a parameter. This array "
						  + "will be populated with the most-recently completed StepExecution(s) for each corresponding step.",
				notes = "See 3.d."
			)
		},
		apiRefs = {	@APIRef(className="javax.batch.api.Decider", methodNames={"decide"}) },
		issueRefs = {"https://github.com/WASdev/standards.jsr352.tck/issues/33", "https://java.net/bugzilla/show_bug.cgi?id=5780"},
		strategy = "JobExecution1: step1 completes, Decider receives this StepExecution, and the job is forced to stop. "
				 + "JobExecution2: step1 does not re-execute (allow-start-if-complete=false), verify that the Decider receives "
				 + "the same StepExecution as in JobExecution1, and force the job to stop. "
				 + "JobExecution3: step1 re-executes (allow-start-if-complete=true), verify that the Decider receives a new StepExecution "
				 + "for step1, and check that the job completes. "
	)
	@Test
	@org.junit.Test
	public void testDeciderReceivesCorrectStepExecutionAfterStep() throws Exception {
		String METHOD = "testDeciderReceivesCorrectStepExecutionAfterStep";
		
		try {
			/* Note: The Job Exit Status on each execution is the Decider return value,
			 * which is set up to indicate which steps and step executions it received */
			
			final String NEW_STEP_EXECUTION_ID = "\\d+;?"; //to be used in regex matching
			
			//Job Execution 1
			Reporter.log("Build job parameters for job execution 1.<p>");
			Properties executionParameters = new Properties();
			executionParameters.setProperty("allow.start.if.complete", "false");
			executionParameters.setProperty("decider.stop.on", "*");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec1 = jobOp.startJobAndWaitForResult("decider_receives_correct_step_execution_after_step", executionParameters);
			String jobExec1Step1Exec = jobExec1.getExitStatus();
			assertWithMessage("Expected Exit Status to be set by Decider", jobExec1Step1Exec.matches("step1:" + NEW_STEP_EXECUTION_ID));
			assertWithMessage("Expected job to be STOPPED after first execution", BatchStatus.STOPPED, jobExec1.getBatchStatus());
			
			//Job Execution 2
			Reporter.log("Don't change job parameters for job execution 2.<p>");
			
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec1.getExecutionId(), executionParameters);
			String jobExec2Step1Exec = jobExec2.getExitStatus();
			assertWithMessage("Execution for step1 should be the same in jobExec2 as in jobExec1", jobExec1Step1Exec, jobExec2Step1Exec);
			assertWithMessage("Expected job to be STOPPED after second execution", BatchStatus.STOPPED, jobExec2.getBatchStatus());
			
			//Job Execution 3
			Reporter.log("Build job parameters for job execution 3.<p>");
			executionParameters.setProperty("allow.start.if.complete", "true");
			executionParameters.setProperty("decider.stop.on", "NONE");
			
			JobExecution jobExec3 = jobOp.restartJobAndWaitForResult(jobExec2.getExecutionId(), executionParameters);
			String jobExec3Step1Exec = jobExec3.getExitStatus();
			assertWithMessage("Execution for step1 should be different in jobExec3 than in jobExec2", !jobExec2Step1Exec.equals(jobExec3Step1Exec));
			assertWithMessage("Expected Exit Status to be set by Decider", jobExec3Step1Exec.matches("step1:" + NEW_STEP_EXECUTION_ID));
			assertWithMessage("Expected job to be COMPLETED after third execution", BatchStatus.COMPLETED, jobExec3.getBatchStatus());
			
		} catch(Exception e) {
			handleException(METHOD, e);
		}		
	}
	
	@TCKTest(
		versions = {"1.1.WORKING"},
		assertions = {"A Decider that follows a split-flow is passed the most recent StepExecution for each flow's last step."},
		specRefs = {
			@SpecRef(
				version = "1.0", section = "9.6",
				citations = "The decide method sets a new exit status for a job. It receives an array of StepExecution objects as input. These "
						  + "StepExecution objects represent the execution element that transitions to this decider as follows: [...] Split [...] "
						  + "When the transition is from a split, the decide method receives a StepExecution from each flow defined to the split as input.",
				notes = "API for Decider"
			),
			@SpecRef(
				version = "1.0RevA", section = "10.8.4",
				citations = { "The Decider's \"decide\" method is passed a StepExecution array as a parameter. This array "
						    + "will be populated with the most-recently completed StepExecution(s) for each corresponding step.",
						  	  "A single decision following a split could even have a mix of old, new StepExecution(s) in the same array." },
				notes = "See 3.d."
			)
		},
		apiRefs = {	@APIRef(className="javax.batch.api.Decider", methodNames={"decide"}) },
		issueRefs = {"https://github.com/WASdev/standards.jsr352.tck/issues/33", "https://java.net/bugzilla/show_bug.cgi?id=5780"},
		strategy = "JobExecution1: split1flow1step1 and split1flow2step1 complete, Decider receives a StepExecution for both steps, and force the job to stop. "
				 + "JobExecution2: neither step re-executes (allow-start-if-complete=false for both steps), verify that the Decider receives the same StepExecutions "
				 + "as in JobExecution1, and force the job to stop. "
				 + "JobExecution3: both steps re-execute (allow-start-if-complete=true for both steps), verify that the Decider receives new StepExecutions for both "
				 + "steps, and force the job to stop. "
				 + "JobExecution4: only 1 step re-executes (allow-start-if-complete=true only for split1flow2step1), verify that the Decider receives the correct "
				 + "mix of old and new StepExecutions, and check that the job completes."
	)
    //This test exposes bug (https://github.com/WASdev/standards.jsr352.jbatch/issues/48) in the RI, disable as needed
	@Test/*(enabled=false)*/ 
	@org.junit.Test
	public void testDeciderReceivesCorrectStepExecutionsAfterSplit() throws Exception {
		String METHOD = "testDeciderReceivesCorrectStepExecutionsAfterSplit";
		
		try {
			/* Note: The Job Exit Status on each execution is the Decider return value,
			 * which is set up to indicate which steps and step executions it received */
			
			final String NEW_STEP_EXECUTION_ID = "\\d+;?"; //to be used in regex matching
			
			//Job Execution 1
			Reporter.log("Build job parameters for job execution 1.<p>");
			Properties executionParameters = new Properties();
			executionParameters.setProperty("split1flow1step1.allow.start.if.complete", "false");
			executionParameters.setProperty("split1flow2step1.allow.start.if.complete", "false");
			executionParameters.setProperty("decider.stop.on", "*");

			Reporter.log("Invoke startJobAndWaitForResult<p>");
			JobExecution jobExec1 = jobOp.startJobAndWaitForResult("decider_receives_correct_step_executions_after_split", executionParameters);
			
			String jobExec1ExitStatus = jobExec1.getExitStatus();
			String[] jobExec1StepExecs = jobExec1ExitStatus.split(";");
			String jobExec1Split1Flow1Step1 = jobExec1StepExecs[0];
			String jobExec1Split1Flow2Step1 = jobExec1StepExecs[1];
			
			assertWithMessage("Expected Exit Status to be set by Decider", jobExec1ExitStatus.matches("split1flow1step1:" + NEW_STEP_EXECUTION_ID + "split1flow2step1:" + NEW_STEP_EXECUTION_ID));
			assertWithMessage("Expected job to be STOPPED after first execution", BatchStatus.STOPPED, jobExec1.getBatchStatus());
			
			//Job Execution 2
			Reporter.log("Don't change any job parameters for job execution 2.<p>");
			
			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec2 = jobOp.restartJobAndWaitForResult(jobExec1.getExecutionId(), executionParameters);
			
			String jobExec2ExitStatus = jobExec2.getExitStatus();
			String[] jobExec2StepExecs = jobExec2ExitStatus.split(";");
			String jobExec2Split1Flow1Step1 = jobExec2StepExecs[0];
			String jobExec2Split1Flow2Step1 = jobExec2StepExecs[1];
			
			StringBuffer explanation = new StringBuffer("\n-----------------------------------\nThis is a known failure and expected result when running the Maven build.\nThe cause is RI bug: https://github.com/WASdev/standards.jsr352.jbatch/issues/48\n");
			explanation.append("But there's no question from the spec perspective that this test SHOULD pass on your implementation or any other implementation.\n");
			explanation.append("So it's currently included in the TCK even though it will fail the Maven build currently).\n-----------------------------------\n");

			assertWithMessage(explanation.toString() + "Execution for split1flow1step1 in jobExec2 should be the same as in jobExec1", jobExec1Split1Flow1Step1, jobExec2Split1Flow1Step1);
			assertWithMessage("Execution for split1flow2step1 in jobExec2 should be the same as in jobExec1", jobExec1Split1Flow2Step1, jobExec2Split1Flow2Step1);
			assertWithMessage("Expected job to be STOPPED after second execution", BatchStatus.STOPPED, jobExec2.getBatchStatus());
			
			//Job Execution 3
			Reporter.log("Build job parameters for job execution 3.<p>");
			executionParameters.setProperty("split1flow1step1.allow.start.if.complete", "true");
			executionParameters.setProperty("split1flow2step1.allow.start.if.complete", "true");
			
			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec3 = jobOp.restartJobAndWaitForResult(jobExec2.getExecutionId(), executionParameters);
			
			String jobExec3ExitStatus = jobExec3.getExitStatus();
			String[] jobExec3StepExecs = jobExec3ExitStatus.split(";");
			String jobExec3Split1Flow1Step1 = jobExec3StepExecs[0];
			String jobExec3Split1Flow2Step1 = jobExec3StepExecs[1];
			
			assertWithMessage("Execution for split1flow1step1 in jobExec3 should be different than in jobExec2", !jobExec2Split1Flow1Step1.equals(jobExec3Split1Flow1Step1));
			assertWithMessage("Execution for split1flow2step1 in jobExec3 should be different than in jobExec2", !jobExec2Split1Flow2Step1.equals(jobExec3Split1Flow2Step1));
			assertWithMessage("Expected Exit Status to be set by Decider", jobExec3ExitStatus.matches("split1flow1step1:" + NEW_STEP_EXECUTION_ID + "split1flow2step1:" + NEW_STEP_EXECUTION_ID));
			assertWithMessage("Expected job to be STOPPED after third execution", BatchStatus.STOPPED, jobExec3.getBatchStatus());
			
			//Job Execution 4
			Reporter.log("Build job parameters for job execution 4.<p>");
			executionParameters.setProperty("split1flow1step1.allow.start.if.complete", "false");
			executionParameters.setProperty("decider.stop.on", "NONE");
			
			Reporter.log("Invoke restartJobAndWaitForResult<p>");
			JobExecution jobExec4 = jobOp.restartJobAndWaitForResult(jobExec3.getExecutionId(), executionParameters);
			
			String jobExec4ExitStatus = jobExec4.getExitStatus();
			String[] jobExec4StepExecs = jobExec4ExitStatus.split(";");
			String jobExec4Split1Flow1Step1 = jobExec4StepExecs[0];
			String jobExec4Split1Flow2Step1 = jobExec4StepExecs[1];
			
			assertWithMessage("Execution for split1flow1step1 in jobExec4 should be the same as in jobExec3", jobExec3Split1Flow1Step1, jobExec4Split1Flow1Step1);
			assertWithMessage("Execution for split1flow2step1 in jobExec4 should be different than in jobExec3", !jobExec3Split1Flow2Step1.equals(jobExec4Split1Flow2Step1));
			assertWithMessage("Expected job to be COMPLETED after fourth execution", BatchStatus.COMPLETED, jobExec4.getBatchStatus());
						
		} catch(Exception e) {
			handleException(METHOD, e);
		}		
	}
	
	private static void handleException(String methodName, Exception e) throws Exception {
		Reporter.log("Caught exception: " + e.getMessage()+"<p>");
		Reporter.log(methodName + " failed<p>");
		throw e;
	}

}
