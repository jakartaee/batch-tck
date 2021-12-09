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
		
		return "OK";
	}


	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}


}
