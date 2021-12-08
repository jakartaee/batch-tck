package com.ibm.jbatch.tck.artifacts.cdi;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;

public class NonCDIBeanBatchlet implements Batchlet {

	private class BatchPropertyLiteral extends AnnotationLiteral<BatchProperty> implements BatchProperty {

		private String name;

		BatchPropertyLiteral(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}
	}


	@Override
	public String process() throws Exception {
		CDI<Object> cdi = CDI.current();
		
		JobContext jobCtx = cdi.select(JobContext.class).get();
		StepContext stepCtx = cdi.select(StepContext.class).get();
		String prop1Val =  cdi.select(String.class, new BatchPropertyLiteral("prop1")).get();
		String prop2Val =  cdi.select(String.class, new BatchPropertyLiteral("prop2")).get();


		appendExitStatus(jobCtx, jobCtx.getExecutionId() + ":" + stepCtx.getStepName() + ":" + prop1Val + ":" + prop2Val);

		return "OK";
	}
	


	private void appendExitStatus(JobContext jobCtx, String toAppend) {
		String es = jobCtx.getExitStatus();
		if (es == null) {
			jobCtx.setExitStatus(toAppend);
		} else {
			jobCtx.setExitStatus(es + "," + toAppend);
		}
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
