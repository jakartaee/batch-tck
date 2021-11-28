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
@Named("CDIDependentScopedBatchletProps")
public class DependentScopedBatchletProps implements Batchlet {

	@Inject @BatchProperty(name="field1") String field1;
	@Inject AppScopedTestBean appScoped;
	@Inject DependentScopedTestBean dependentScoped;
	@Inject JobContext jobCtx; 
	
	private String m1;

	@Inject  
	public void setMethod1(@BatchProperty(name="method1") String method1) {
		m1 = method1;
	}


	
	@Override
	public String process() throws Exception {
		if (dependentScoped == null || appScoped == null) {
			throw new Exception("TEST FAILED");
		} else {
			jobCtx.setExitStatus(field1 + ":" + m1);
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
