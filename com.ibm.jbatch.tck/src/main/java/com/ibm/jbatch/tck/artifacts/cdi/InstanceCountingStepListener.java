package com.ibm.jbatch.tck.artifacts.cdi;

import jakarta.batch.api.listener.AbstractStepListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class InstanceCountingStepListener extends AbstractStepListener {
	
	public int count = 0;
	
	@Inject JobContext jobCtx;

	@Override
	public void beforeStep() throws Exception {
		jobCtx.setTransientUserData(++count);
	}

	@Override
	public void afterStep() throws Exception {
		jobCtx.setTransientUserData(++count);
	}

}
