package com.ibm.jbatch.tck.cdi;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class DependentScopedTestBean {
	
	@Inject 
	JobContext jobCtx;

	@Inject 
	StepContext stepCtx;

	@Inject @BatchProperty(name="prop1") String prop1;

	public long getJobContextExecId() {
		return jobCtx.getExecutionId();
	}

	public long getStepContextExecId() {
		return stepCtx.getStepExecutionId();
	}

	public String getProp1Val() {
		return prop1;
	}

}
