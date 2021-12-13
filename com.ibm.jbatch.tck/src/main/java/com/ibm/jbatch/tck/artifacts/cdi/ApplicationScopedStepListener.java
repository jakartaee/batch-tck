package com.ibm.jbatch.tck.artifacts.cdi;

import jakarta.batch.api.listener.AbstractStepListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("CDIApplicationScopedStepListener")
public class ApplicationScopedStepListener extends AbstractStepListener {
	
	int count = 0;

	@Override
	public void beforeStep() {
		count++;
	}
		
	public int getCount() {
		return ++count;
	}

}
