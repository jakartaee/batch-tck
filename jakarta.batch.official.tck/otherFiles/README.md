# Jakarta Batch TCK 

This is the Technology Compatibility Kit (TCK) for Jakarta Batch.  

The Jakarta Batch specification describes the job specification language,
Java programming model, and runtime environment for Jakarta Batch applications.

This distribution, as a whole, is licensed under the terms of the Eclipse Foundation Technology Compatibility Kit License - v 1.0 (see LICENSE.txt).

## Key Documenation Within

Two important components to note are each located in the **doc/** subdirectory: 

1. **doc/batch-tck-reference-guide.adoc**

The TCK Reference Guide - Describes the components of the TCK in more detail and also more explicitly enumerates the steps required to pass the TCK and claim compatibility.

2.  **doc/how-to-run-tck-against-jbatch-script.sh**

The instructions on how to run the TCK against com.ibm.jbatch (as a "Compatible Implementation"), which played a role in verifying the TCK itself.

## Contents in more detail

This distribution consists of:

* artifacts/

  * TCK binaries and source, packaged as jars
   -- TestNG suite.xml file for running the TCK

* doc/

  * Reference guide for the TCK

* lib/

  * Dependencies for running the TCK

* /  

  * The archive root contains files used in the execution of the tests as well as other information files - legal, coverage statement, etc.

Consult the TCK Reference Guide for more detailed information on the TCK contents.