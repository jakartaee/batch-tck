package com.ibm.jbatch.tck.cdi;

import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentScopedTestBean {
	
	public String getTimestamp() {
		return Long.toString(System.nanoTime());
	}

}
