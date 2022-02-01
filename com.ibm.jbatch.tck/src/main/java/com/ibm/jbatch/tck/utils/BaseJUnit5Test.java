package com.ibm.jbatch.tck.utils;

import ee.jakarta.tck.batch.util.extensions.VehicleInvocationInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VehicleInvocationInterceptor.class)
public class BaseJUnit5Test {
    
    @BeforeEach
    public void setUpReporter(TestReporter reporter) {
        Reporter.reporterRef.set(reporter);
    }

    @AfterEach
    public void cleanUpReporter() {
        Reporter.reporterRef.remove();
    }
}
