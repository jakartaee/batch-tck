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

import java.io.Serializable;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.context.JobContext;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

import com.ibm.jbatch.tck.artifacts.basicchunk.BasicItem;

/*NOTE: Code for this class is taken substantially from basicchunk.BasicReader*/
@javax.inject.Named("PUDPartitionReader")
public class PUDPartitionReader extends AbstractItemReader {

    @Inject 
    JobContext jobCtx;
    
    @Inject 
    StepContext stepCtx;
    
    @Inject @BatchProperty(name="partition.number")
    String partitionNumber;
    
    @Inject @BatchProperty(name="execution.number")
    String executionNumber;
    
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

    private String PUDString;
    private int currentItemId = -1;
    private BasicItem currentItem = null;
    
    @Override
    public void open(Serializable checkpoint) {
    	
        PUDString = "PUD for Partition: " +partitionNumber;
        
        // Set on the first execution; on later executions it will need to be obtained
        // from the job repository's persistent store.
        if (checkpoint == null) {
            stepCtx.setPersistentUserData(PUDString);
        }
        
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
    	
        if (executionNumber.equals("2")) {
            if (!stepCtx.getPersistentUserData().equals(PUDString)) {
                throw new Exception("BadPersistentUserData: PUD for partition "+partitionNumber+" is expected to be "+PUDString+", but found "+stepCtx.getPersistentUserData());
            }
        }
    	
        //Code below is take from BasicReader
    	
        /* Note that BasicReader has no concept of rolling back after a retryable exception is thrown.
         * Example: chunk size is 2, we plan to read 10 items (#0-#9), but a retryable  exception is thrown while reading item #1 
         * In this case, the reader goes on to read #2 when it should be rolling back to #0, and so the writer will never receive 
         * #0, even though it was previously read successfully */
        
        currentItemId++;
        
        if (currentItemId < numberOfItemsToBeRead) {
            currentItem = new BasicItem(currentItemId);
            if (readerExceptionShouldBeThrownForCurrentItem()) {
                //set the job exit status so we can determine which exception was last thrown
                jobCtx.setExitStatus("Exception:Item#" + currentItem.getId());
                throw new Exception("Exception thrown for item " + currentItem.getId());
            }        	
            currentItem.setRead(true);
            return currentItem;
        }
        
        return null;
    }

    private boolean readerExceptionShouldBeThrownForCurrentItem() {

        for (int i: throwReaderExceptionForTheseItems) {
            if (currentItem.getId()==i) { return true; }
        }
        
        return false;
    }
}
