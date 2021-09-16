package com.ibm.jbatch.tck.cdi;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppScopedTestBean {

	public String getTimestamp() {
		return Long.toString(System.nanoTime());
	}

}
