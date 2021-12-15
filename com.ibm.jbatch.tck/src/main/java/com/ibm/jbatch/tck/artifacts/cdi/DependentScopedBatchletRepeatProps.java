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

/**
 * Prove that a single batch property can be injected multiple times, in multiple ways
 * (field vs method parm etc.)
 */
@Dependent
@Named("CDIDependentScopedBatchletRepeatProps")
public class DependentScopedBatchletRepeatProps implements Batchlet {

	@Inject JobContext jobCtx;

	@Inject @BatchProperty String prop1;
	@Inject @BatchProperty String prop2;
	@Inject @BatchProperty(name="prop1") String prop3;
	
	private String m1;
	private String m2;
	private String m3;
	private String m4;
	private String m5;
	private String m6;

	private String c1;
	private String c2;
	private String c3;

	@Inject
	DependentScopedBatchletRepeatProps(@BatchProperty(name="prop1") String c1, @BatchProperty(name="prop2") String c2,  @BatchProperty(name="prop1") String c3) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
	}

	@Inject  
	public void setMethod1(@BatchProperty(name="prop1") String m1, @BatchProperty(name="prop2") String m2) {
		this.m1 = m1;
		this.m2 = m2;
	}
	
	@Inject  
	public void setMethod2(@BatchProperty(name="prop1") String m3) {
		this.m3 = m3;
	}
	
	@Inject  
	public void setMethod2(@BatchProperty(name="prop1") String m4, @BatchProperty(name="prop1") String m5,  @BatchProperty(name="prop1") String m6) {
		this.m4 = m4;
		this.m5 = m5;
		this.m6 = m6;
	}

	@Override
	public String process() throws Exception {
		jobCtx.setExitStatus(String.join(":", prop1, prop2, prop3, c1, c2, c3, m1, m2, m3, m4, m5, m6));
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
