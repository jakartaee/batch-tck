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

	@Inject @BatchProperty(name="f1") String field1;
	@Inject @BatchProperty String f2;
	@Inject AppScopedTestBean appScoped;
	@Inject DependentScopedTestBean dependentScoped;
	@Inject JobContext jobCtx; 
	
	private String method1;
	private String method2;

	private String ctor1;
	private String ctor2;

	@Inject
	DependentScopedBatchletProps(@BatchProperty(name="c1") String ctor1, @BatchProperty(name="c2") String c2) {
		this.ctor1 = ctor1;
		this.ctor2 = c2;
	}

	@Inject  
	public void setMethod1(@BatchProperty(name="m1") String method1) {
		this.method1 = method1;
	}
	
	@Inject  
	public void setMethod2(@BatchProperty(name="m2") String m2) {
		this.method2 = m2;
	}
	
	@Override
	public String process() throws Exception {
		if (dependentScoped == null || appScoped == null) {
			throw new Exception("TEST FAILED");
		} else {
			jobCtx.setExitStatus(String.join(":", ctor1, ctor2, field1, f2, method1, method2));
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
