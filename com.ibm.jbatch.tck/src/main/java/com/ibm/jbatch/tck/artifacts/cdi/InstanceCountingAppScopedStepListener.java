package com.ibm.jbatch.tck.artifacts.cdi;

import jakarta.batch.api.listener.AbstractStepListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class InstanceCountingAppScopedStepListener extends AbstractStepListener {
	
	private int count = 0;
	
	@Inject JobContext jobCtx;

	@Override
	public void beforeStep() throws Exception {
		jobCtx.setTransientUserData(++count);
	}

}
