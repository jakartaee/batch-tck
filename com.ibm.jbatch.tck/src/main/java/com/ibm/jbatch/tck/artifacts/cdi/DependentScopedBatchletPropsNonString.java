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
@Named("CDIDependentScopedBatchletPropsNonString")
public class DependentScopedBatchletPropsNonString implements Batchlet {

	@Inject JobContext jobCtx; 
	
	@Inject @BatchProperty(name="stringProp") String stringProp;
	@Inject @BatchProperty(name="booleanProp1") Boolean booleanProp1;
	@Inject @BatchProperty(name="booleanProp2") Boolean booleanProp2;
//	@Inject @BatchProperty(name="byteProp1") byte byteProp1;
//	@Inject @BatchProperty(name="byteProp2") Byte byteProp2;
	@Inject @BatchProperty(name="doubleProp1") Double doubleProp1;
	@Inject @BatchProperty(name="doubleProp2") Double doubleProp2;
	@Inject @BatchProperty(name="floatProp1") Float floatProp1;
	@Inject @BatchProperty(name="floatProp2") Float floatProp2;
	@Inject @BatchProperty(name="intProp1") Integer intProp1;
	@Inject @BatchProperty(name="intProp2") Integer intProp2;
	@Inject @BatchProperty(name="longProp1") Long longProp1;
	@Inject @BatchProperty(name="longProp2") Long longProp2;
	@Inject @BatchProperty(name="shortProp1") Short shortProp1;
	@Inject @BatchProperty(name="shortProp2") Short shortProp2;


	String expectedStringProp = "HappyBatchProperties";
	Boolean expectedBooleanProp1 = Boolean.valueOf("true").booleanValue();
	Boolean expectedBooleanProp2 = Boolean.valueOf("Nope");
//	byte expectedByteProp1 = Byte.valueOf("100").byteValue();
//	Byte expectedByteProp2 = Byte.valueOf("@");
	Double expectedDoubleProp1 = Double.valueOf("234.432").doubleValue();
	Double expectedDoubleProp2 = Double.valueOf("123.321");
	Float expectedFloatProp1 = Float.valueOf("11234.432F").floatValue();
	Float expectedFloatProp2 = Float.valueOf("11123.321F");
	Integer expectedIntProp1 = 7777;
	Integer expectedIntProp2 = 8888;
	Long expectedLongProp1 = 1234567890123L;
	Long expectedLongProp2 = 12345678901234L;
	Short expectedShortProp1 = 333;
	Short expectedShortProp2 = 444;

	
	@Override
	public String process() throws Exception {
		verifyProperties();
		return "OK";
	}

	private void error(String propName, Object propValue) throws Exception {
		String errorMsg = "FAIL: Found " + propName + " of: " + propValue;
		jobCtx.setExitStatus(errorMsg); throw new Exception(errorMsg);
	}

	private void verifyProperties() throws Exception {
		if (!stringProp.equals(expectedStringProp)) {
			error("stringProp", stringProp);
		} else if (!booleanProp1.equals(expectedBooleanProp1)) {
			error("booleanProp1", booleanProp1);
		} else if (!booleanProp2.equals(expectedBooleanProp2)) {
			error("booleanProp2", booleanProp2);
//		} else if (byteProp1 != expectedByteProp1) {
//			error("byteProp1", byteProp1);
//		} else if (!byteProp2.equals(expectedByteProp2)) {
//			error("byteProp2", byteProp2);
		} else if (!doubleProp1.equals(expectedDoubleProp1)) {
			error("doubleProp1", doubleProp1);
		} else if (!doubleProp2.equals(expectedDoubleProp2)) {
			error("doubleProp2", doubleProp2);
		} else if (!floatProp1.equals(expectedFloatProp1)) {
			error("floatProp1", floatProp1);
		} else if (!floatProp2.equals(expectedFloatProp2)) {
			error("floatProp2", floatProp2);
		} else if (!intProp1.equals(expectedIntProp1)) {
			error("intProp1", intProp1);
		} else if (!intProp2.equals(expectedIntProp2)) {
			error("intProp2", intProp2);
		} else if (!longProp1.equals(expectedLongProp1)) {
			error("longProp1", longProp1);
		} else if (!longProp2.equals(expectedLongProp2)) {
			error("longProp2", longProp2);
		} else if (!shortProp1.equals(expectedShortProp1)) {
			error("shortProp1", shortProp1);
		} else if (!shortProp2.equals(expectedShortProp2)) {
			error("shortProp2", shortProp2);
		}
		jobCtx.setExitStatus("GOOD");
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
