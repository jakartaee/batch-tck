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

import java.io.Serializable;
import java.util.List;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

@javax.inject.Named("basicWriter")
public class BasicWriter extends AbstractItemWriter {

	@Inject
    JobContext jobCtx;
	
	@Inject
    @BatchProperty(name = "throw.writer.exception.for.these.items")
    String injectedThrowWriterExceptionForTheseItems;
	//Default: don't throw any exceptions
    private int[] throwWriterExceptionForTheseItems = {};
	
	private BasicItem currentItem = null;
    
	@Override
	public void open(Serializable cpd) throws Exception {
		
		if (injectedThrowWriterExceptionForTheseItems != null) {
			String[] exceptionsStringArray = injectedThrowWriterExceptionForTheseItems.split(",");
			throwWriterExceptionForTheseItems = new int[exceptionsStringArray.length];
            for (int i = 0; i < exceptionsStringArray.length; i++) {
            	throwWriterExceptionForTheseItems[i] = Integer.parseInt(exceptionsStringArray[i]);
            }
		}
	}
	
	@Override
	public void writeItems(List<Object> items) throws Exception {
		
		for (Object item: items) {
			currentItem = (BasicItem)item;
			
			if (writerExceptionShouldBeThrownForCurrentItem()) {
				//set the job exit status so we can determine which exception was last thrown
            	jobCtx.setExitStatus("BasicWriterException:Item#" + currentItem.getId());
				throw new BasicWriterException("BasicWriterException thrown for item " + currentItem.getId());
	        }
			
			currentItem.setWritten(true);			
		}
	}
	
	private boolean writerExceptionShouldBeThrownForCurrentItem() {
    	for (int i: throwWriterExceptionForTheseItems) {
    		if (currentItem.getId()==i) { return true; }
    	}   	
        return false;
    }
}
