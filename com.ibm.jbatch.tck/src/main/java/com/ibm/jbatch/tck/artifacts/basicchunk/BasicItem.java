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

public class BasicItem {

    private int id;
    private boolean read;
    private boolean processed;
    private boolean written;

    public BasicItem(int id) {
        this.id = id;
        this.read = false;
        this.processed = false;
        this.written = false;
    }

    public int getId() {
        return id;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isProcessed() {
        return processed;
    }

    public boolean isWritten() {
        return written;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public void setWritten(boolean written) {
        this.written = written;
    }
}
