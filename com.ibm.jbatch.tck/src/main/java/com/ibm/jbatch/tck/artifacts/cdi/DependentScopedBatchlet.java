package com.ibm.jbatch.tck.artifacts.cdi;

import java.io.StringWriter;
import java.util.Properties;

import com.ibm.jbatch.tck.cdi.AppScopedTestBean;
import com.ibm.jbatch.tck.cdi.DependentScopedTestBean;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Dependent
@Named("CDIDependentScopedBatchlet")
public class DependentScopedBatchlet implements Batchlet {

	@Inject @BatchProperty(name="xyz") String xyz;
	@Inject AppScopedTestBean appScoped;
	@Inject DependentScopedTestBean dependentScoped;
	@Inject JobContext jobCtx; 
	
//	@Override
//	public String process() throws Exception {
//		Properties p = new Properties();
//		if (dependentScoped != null) {
//			p.setProperty("dependent", dependentScoped.getTimestamp());
//		}
//		if (appScoped != null) {
//			p.setProperty("app", appScoped.getTimestamp());
//		}
//		jobCtx.setExitStatus(getPropertyAsString(p));
//		return "GOOD";
//	}
	
	@Override
	public String process() throws Exception {
		if (dependentScoped != null && appScoped != null) {
			jobCtx.setExitStatus("GOOD");
		} else {
			jobCtx.setExitStatus("FAIL: " + " dep = " + dependentScoped + ", app = " + appScoped);
		}
		return "OK";
	}


	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public static String getPropertyAsString(Properties prop) throws Exception {
	    StringWriter writer = new StringWriter();
	    prop.store(writer, "");
	    return writer.getBuffer().toString();
	}
}
