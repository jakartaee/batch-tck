/*
 * Copyright 2016 International Business Machines Corp.
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
package com.ibm.jbatch.tck.artifacts.specialized;

import java.util.Arrays;
import java.util.Comparator;

import javax.batch.api.BatchProperty;
import javax.batch.api.Decider;
import javax.batch.runtime.StepExecution;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

@javax.inject.Named("deciderReceivesCorrectStepExecutionsDecider")
public class DeciderReceivesCorrectStepExecutionsDecider implements Decider {

    @Inject
    JobContext jobCtx;

    @Inject
    @BatchProperty(name = "expected.number.of.step.executions")
    String numOfStepExecs;

    @Override
    public String decide(StepExecution[] stepExecutions) throws Exception {
        if (stepExecutions.length != Integer.parseInt(numOfStepExecs)) {
            throw new Exception("Expecting stepExecutions array of size " + numOfStepExecs + ", found one of size = " + stepExecutions.length);
        }

		/*we sort the stepExecutions to guarantee that the decider exit status, made up of the concatenated
		step executions, will have the same order of step executions on subsequent job executions*/
        sortStepExecutionsByStepName(stepExecutions);

        /*
         * exitStatus will be in the format:
         *   "split1flow1step1:1235;split1flow1step2:1236;split1flow2step1:1237;"
         * split the exitStatus at each ";" for each stepExecution
         * split a stepExecution at ":" to get the stepName and stepExecutionId
         */
        String exitStatus = "";
        for (StepExecution stepExecution : stepExecutions) {
            exitStatus += stepExecution.getStepName() + ":" + stepExecution.getStepExecutionId() + ";";
        }
        return exitStatus; //Decider return value should be set as the Job Exit Status
    }

    private void sortStepExecutionsByStepName(StepExecution[] stepExecutions) {
        Arrays.sort(stepExecutions, new Comparator<StepExecution>() {
            @Override
            public int compare(StepExecution se1, StepExecution se2) {
                return se1.getStepName().compareTo(se2.getStepName());
            }
        });
    }
}
