package com.ibm.jbatch.tck.artifacts.cdi;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("CDIApplicationScopedBatchlet")
public class ApplicationScopedBatchlet implements Batchlet {

	@Inject @BatchProperty(name="prop1") Instance<String> prop1Inst;
	@Inject @BatchProperty Instance<String> prop2;  // Default to field name
	@Inject Instance<JobContext> jobCtxInst;
	@Inject Instance<StepContext> stepCtxInst;


	@Override
	public String process() throws Exception {
		JobContext jobCtx = jobCtxInst.get();
		StepContext stepCtx = stepCtxInst.get();
		String prop1Val = prop1Inst.get();
		String prop2Val = prop2.get();
		
		appendExitStatus(jobCtx, jobCtx.getExecutionId() + ":" + stepCtx.getStepName() + ":" + prop1Val + ":" + prop2Val);

		return "OK";
	}

	private void appendExitStatus(JobContext jobCtx, String toAppend) {
		String es = jobCtx.getExitStatus();
		if (es == null) {
			jobCtx.setExitStatus(toAppend);
		} else {
			jobCtx.setExitStatus(es + "," + toAppend);
		}
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
