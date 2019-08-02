# Jakarta Batch TCK 

This is the Technology Compatibility Kit (TCK) for Jakarta Batch.  

The Jakarta Batch specification describes the job specification language,
Java programming model, and runtime environment for Jakarta Batch applications.

This distribution, as a whole, is licensed under the terms of the Eclipse Foundation Technology Compatibility Kit License - v 1.0 (see **LICENSE_EFTL.md** included within this zip file).

## Key Documentation Within

Two important components to note are each located in the **doc/** subdirectory: 

1. **Jakarta Batch TCK Reference Guide**

    This document, e.g. **batch-tck-reference-guide-1.0.2.pdf/html**, describes the components of the TCK in more detail and also more explicitly enumerates the steps required to pass the TCK, claim compatibility, as well as other processes like how to file a challenge against the TCK for a test that you believe to be invalid.

2.  **Execution of TCK against jbatch as compatible implementation**

    This script, **how-to-run-tck-against-jbatch-script.sh**, automates the execution of the TCK against the "jbatch" implementation.  It is commented such that it could be modified to be re-executed later in a different environment.  The execution of the TCK against "jbatch" as a **Compatible Implementation** plays a key role in verifying the TCK itself.

## Contents in more detail

This distribution consists of:

* artifacts/

  * TCK binaries and source, packaged as jars
   -- TestNG suite.xml file for running the TCK

* doc/

  * Reference guide for the TCK; plus script for executing TCK against "jbatch" 

* lib/

  * Dependencies for running the TCK

* /  

  * The archive root contains files used in the execution of the tests as well as other information files - legal, coverage statement, etc.

Consult the **Jakarta Batch TCK Reference Guide** for more detailed information on the TCK contents.
