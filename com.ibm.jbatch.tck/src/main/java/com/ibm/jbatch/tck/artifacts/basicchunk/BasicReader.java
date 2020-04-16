/*
 * Copyright 2016, 2020 International Business Machines Corp. and others
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
package com.ibm.jbatch.tck.artifacts.basicchunk;

import java.io.Serializable;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;

@jakarta.inject.Named("basicReader")
public class BasicReader extends AbstractItemReader {

    @Inject
    JobContext jobCtx;

    @Inject
    @BatchProperty(name = "number.of.items.to.be.read")
    String injectedNumberOfItemsToBeRead;
    //Default: read 10 items
    private int numberOfItemsToBeRead = 10;

    @Inject
    @BatchProperty(name = "throw.reader.exception.for.these.items")
    String injectedThrowReaderExceptionForTheseItems;
    //Default: don't throw any exceptions
    private int[] throwReaderExceptionForTheseItems = {};

    private int currentItemId = -1;
    private BasicItem currentItem = null;

    @Override
    public void open(Serializable checkpoint) {

        if (injectedNumberOfItemsToBeRead != null) {
            numberOfItemsToBeRead = Integer.parseInt(injectedNumberOfItemsToBeRead);
        }

        if (injectedThrowReaderExceptionForTheseItems != null) {
            String[] exceptionsStringArray = injectedThrowReaderExceptionForTheseItems.split(",");
            throwReaderExceptionForTheseItems = new int[exceptionsStringArray.length];
            for (int i = 0; i < exceptionsStringArray.length; i++) {
                throwReaderExceptionForTheseItems[i] = Integer.parseInt(exceptionsStringArray[i]);
            }
        }
    }

    @Override
    public BasicItem readItem() throws Exception {
        /* Note that BasicReader has no concept of rolling back after a retryable exception is thrown.
         * Example: chunk size is 2, we plan to read 10 items (#0-#9), but a retryable  exception is thrown while reading item #1
         * In this case, the reader goes on to read #2 when it should be rolling back to #0, and so the writer will never receive
         * #0, even though it was previously read successfully */

        currentItemId++;

        if (currentItemId < numberOfItemsToBeRead) {
            currentItem = new BasicItem(currentItemId);
            if (readerExceptionShouldBeThrownForCurrentItem()) {
                //set the job exit status so we can determine which exception was last thrown
                jobCtx.setExitStatus("BasicReaderException:Item#" + currentItem.getId());
                throw new BasicReaderException("BasicReaderException thrown for item " + currentItem.getId());
            }
            currentItem.setRead(true);
            return currentItem;
        }

        return null;
    }

    private boolean readerExceptionShouldBeThrownForCurrentItem() {

        for (int i : throwReaderExceptionForTheseItems) {
            if (currentItem.getId() == i) {
                return true;
            }
        }

        return false;
    }
}
