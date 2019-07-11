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
package com.ibm.jbatch.tck.artifacts.basicchunk;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

@javax.inject.Named("basicProcessor")
public class BasicProcessor implements ItemProcessor {
	
	@Inject
    JobContext jobCtx;
	
	@Inject
    @BatchProperty(name = "throw.processor.exception.for.these.items")
    String injectedThrowProcessorExceptionForTheseItems;
	//Default: don't throw any exceptions
    private int[] throwProcessorExceptionForTheseItems = {};
    
    @Inject
    @BatchProperty(name = "filter.out.these.items")
    String injectedFilterOutTheseItems;
	//Default: don't filter out any items
    private int[] filterOutTheseItems = {};
    
	boolean initialized = false;
	private BasicItem currentItem = null;
	
	private void initializeProcessor() {
		if (injectedThrowProcessorExceptionForTheseItems != null) {
			String[] exceptionsStringArray = injectedThrowProcessorExceptionForTheseItems.split(",");
			throwProcessorExceptionForTheseItems = new int[exceptionsStringArray.length];
            for (int i = 0; i < exceptionsStringArray.length; i++) {
            	throwProcessorExceptionForTheseItems[i] = Integer.parseInt(exceptionsStringArray[i]);
            }
		}
		
		if (injectedFilterOutTheseItems != null) {
			String[] filterStringArray = injectedFilterOutTheseItems.split(",");
			filterOutTheseItems = new int[filterStringArray.length];
            for (int i = 0; i < filterStringArray.length; i++) {
            	filterOutTheseItems[i] = Integer.parseInt(filterStringArray[i]);
            }
		}
		
		initialized = true;
	}
	
	@Override
	public BasicItem processItem(Object item) throws Exception {		
		if (!initialized) { initializeProcessor(); }		
		currentItem = (BasicItem)item;
		
		//throwing exception takes precedence over filtering
		if (processorExceptionShouldBeThrownForCurrentItem()) {
			//set the job exit status so we can determine which exception was last thrown
        	jobCtx.setExitStatus("BasicProcessorException:Item#" + currentItem.getId());
			throw new BasicProcessorException("BasicProcessorException thrown for item " + currentItem.getId());
        }
		
		if (currentItemShouldBeFilteredOut()) {
        	return null;
        }
		
		currentItem.setProcessed(true);
		return currentItem;
	}
	
	private boolean processorExceptionShouldBeThrownForCurrentItem() {
    	for (int i: throwProcessorExceptionForTheseItems) {
    		if (currentItem.getId()==i) { return true; }
    	}   	
        return false;
    }
	
	private boolean currentItemShouldBeFilteredOut() {
    	for (int i: filterOutTheseItems) {
    		if (currentItem.getId()==i) { return true; }
    	}    	
        return false;
    }
}
