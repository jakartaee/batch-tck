#####################
# Jakarta Batch TCK #
#####################

The Jakarta Batch specification describes the job specification language,
Java programming model, and runtime environment for Jakarta Batch applications.

This is the TCK for Jakarta Batch

This distribution, as a whole, is licensed under the terms of the Eclipse Foundation Technology Compatibility Kit License - v 1.0 (see LICENSE.txt).

This distribution consists of:

artifacts/
   -- TCK binaries and source, packaged as jars
   -- TestNG suite.xml file for running the TCK

doc/
   -- Reference guide for the TCK

lib/
   -- Dependencies for running the TCK
   
build.xml, sigtest.build.xml
   -- Ant build files used to run TestNG, signature test portions of the TCK

batch-tck.properties, batch-sigtest-tck.properties
   -- Specify the location of required properties here for each of the TestNG, signature test portions of the TCK, respectively
