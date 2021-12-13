package com.ibm.jbatch.tck.artifacts.cdi;

import java.io.StringWriter;
import java.util.Properties;

import com.ibm.jbatch.tck.cdi.AppScopedTestBean;
import com.ibm.jbatch.tck.cdi.DependentScopedTestBean;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Dependent
@Named("CDIDependentScopedBatchletInjectListener")
public class DependentScopedBatchletInjectListener implements Batchlet {

	@Inject ApplicationScopedStepListener listener;
	@Inject JobContext jobCtx;

	@Override
	public String process() throws Exception {
		updateJobExitStatus(Integer.toString(listener.getCount()));
		return "OK";
	}

	private void updateJobExitStatus(String msg) {
		String es = jobCtx.getExitStatus();
		es = (es == null ? "" : es);
		StringBuilder sb = new StringBuilder(es);
		sb.append(msg).append(":");
		jobCtx.setExitStatus(sb.toString());
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
