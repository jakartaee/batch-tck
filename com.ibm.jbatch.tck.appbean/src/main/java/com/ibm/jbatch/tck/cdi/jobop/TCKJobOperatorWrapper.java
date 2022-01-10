/*
 * Copyright 2022 International Business Machines Corp. and others
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.jbatch.tck.cdi.jobop;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import jakarta.batch.operations.JobExecutionAlreadyCompleteException;
import jakarta.batch.operations.JobExecutionIsRunningException;
import jakarta.batch.operations.JobExecutionNotMostRecentException;
import jakarta.batch.operations.JobExecutionNotRunningException;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.JobRestartException;
import jakarta.batch.operations.JobSecurityException;
import jakarta.batch.operations.JobStartException;
import jakarta.batch.operations.NoSuchJobException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.operations.NoSuchJobInstanceException;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;
import jakarta.batch.runtime.StepExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

public class TCKJobOperatorWrapper implements JobOperator {

	@Override
	public Set<String> getJobNames() throws JobSecurityException {
		return BatchRuntime.getJobOperator().getJobNames();
	}

	@Override
	public int getJobInstanceCount(String jobName) throws NoSuchJobException, JobSecurityException {
		return BatchRuntime.getJobOperator().getJobInstanceCount(jobName);
	}

	@Override
	public List<JobInstance> getJobInstances(String jobName, int start, int count)
			throws NoSuchJobException, JobSecurityException {
		return BatchRuntime.getJobOperator().getJobInstances(jobName, start, count);
	}

	@Override
	public List<Long> getRunningExecutions(String jobName) throws NoSuchJobException, JobSecurityException {
		return BatchRuntime.getJobOperator().getRunningExecutions(jobName);
	}

	@Override
	public Properties getParameters(long executionId) throws NoSuchJobExecutionException, JobSecurityException {
		return BatchRuntime.getJobOperator().getParameters(executionId);
	}

	@Override
	public long start(String jobXMLName, Properties jobParameters) throws JobStartException, JobSecurityException {
		return BatchRuntime.getJobOperator().start(jobXMLName, jobParameters);
	}

	@Override
	public long restart(long executionId, Properties restartParameters) throws JobExecutionAlreadyCompleteException,
			NoSuchJobExecutionException, JobExecutionNotMostRecentException, JobRestartException, JobSecurityException {
		return BatchRuntime.getJobOperator().restart(executionId, restartParameters);
	}

	@Override
	public void stop(long executionId)
			throws NoSuchJobExecutionException, JobExecutionNotRunningException, JobSecurityException {
		BatchRuntime.getJobOperator().stop(executionId);
	}

	@Override
	public void abandon(long executionId)
			throws NoSuchJobExecutionException, JobExecutionIsRunningException, JobSecurityException {
		
		BatchRuntime.getJobOperator().abandon(executionId);
	}

	@Override
	public JobInstance getJobInstance(long executionId) throws NoSuchJobExecutionException, JobSecurityException {
		return BatchRuntime.getJobOperator().getJobInstance(executionId);
	}

	@Override
	public List<JobExecution> getJobExecutions(JobInstance instance)
			throws NoSuchJobInstanceException, JobSecurityException {
		return BatchRuntime.getJobOperator().getJobExecutions(instance);
	}

	@Override
	public JobExecution getJobExecution(long executionId) throws NoSuchJobExecutionException, JobSecurityException {
		return BatchRuntime.getJobOperator().getJobExecution(executionId);
	}

	@Override
	public List<StepExecution> getStepExecutions(long jobExecutionId)
			throws NoSuchJobExecutionException, JobSecurityException {
		return BatchRuntime.getJobOperator().getStepExecutions(jobExecutionId);
	}

}
