/*
 * Copyright 2014 International Business Machines Corp.
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
package com.ibm.jbatch.tck.artifacts.inner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.batch.api.chunk.listener.AbstractItemProcessListener;
import jakarta.batch.api.chunk.listener.AbstractItemWriteListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ListenerOnErrorArtifacts {

    private static int failOn = 8;
    private static int max = 10;

    @Named("ListenerOnErrorArtifacts.R")
    public static class R extends AbstractItemReader {

        int i = 0;

        @Override
        public Object readItem() throws Exception {
            if (i < max) {
                return i++;
            } else {
                return null;
            }
        }
    }

    @Named("ListenerOnErrorArtifacts.P")
    public static class P implements ItemProcessor {

        @Inject
        @BatchProperty(name = "process.fail")
        String failString;

        @Override
        public Object processItem(Object item) throws Exception {

            int itemNum = (Integer) item;

            if (Boolean.parseBoolean(failString)) {
                if (itemNum == failOn) {
                    throw new Exception("process fail immediate");
                }
            }

            return 2 * itemNum;
        }
    }

    @Named("ListenerOnErrorArtifacts.W")
    public static class W extends AbstractItemWriter {

        int writeCount = 0;

        @Inject
        @BatchProperty(name = "write.fail")
        String failString;

        @Override
        public void writeItems(List<Object> items) throws Exception {
            writeCount += items.size();

            // Throw if reached threshold
            if (writeCount >= failOn) {
                throw new Exception("process fail immediate");
            }

            // No writing actually done here
        }
    }

    @Named("ListenerOnErrorArtifacts.WL")
    public static class WL extends AbstractItemWriteListener {
        @Inject
        JobContext jobCtx;

        @Override
        public void onWriteError(List<Object> items, Exception e) throws Exception {
            jobCtx.setExitStatus(items.toString());
        }
    }

    @Named("ListenerOnErrorArtifacts.PL")
    public static class PL extends AbstractItemProcessListener {
        @Inject
        JobContext jobCtx;

        @Override
        public void onProcessError(Object item, Exception ex) throws Exception {
            jobCtx.setExitStatus(item.toString());
        }
    }
}
