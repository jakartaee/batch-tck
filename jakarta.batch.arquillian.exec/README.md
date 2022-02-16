<!--- 
Copyright (c) 2021-2022 Contributors to the Eclipse Foundation

See the NOTICE file distributed with this work for additional information regarding copyright 
ownership. Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. You may 
obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
Unless required by applicable law or agreed to in writing, software distributed 
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
OR CONDITIONS OF ANY KIND, either express or implied. See the License for 
the specific language governing permissions and limitations under the License. 
SPDX-License-Identifier: Apache-2.0
--->

# NOTE:  Unofficial Documentation Only

Note that the **Jakarta Batch TCK Reference Guide** document included in the TCK distribution contains the final, official instructions for all aspects of executing the TCK and certifying an implementation as compliant with the specification.

This README contains examples on running against the Arquillian-based TCK tests against specific implementations, (GlassFish and Open Liberty).  Since, naturally, there is no requirement to run the TCK against specific implementations, the instructions below should be taken just as examples to help get started and get familiar with Arquillian-based execution of the Batch TCK, and not as official, required TCK instructions.

## How to run the TCK against GlassFish

NOTE: The TCK doesn't pass with any existing GlassFish release yet. 
The work on GlassFish 7.x has only started and support for Batch 2.1 has not been implemented there yet.

First, install GlassFish on the same machine and then start it with the default configuration:

```
asadmin start-domain domain1
```

Alternatively, create a brand-new domain and start it:

```
asadmin create-domain --checkports=false --nopassword batch
asadmin start-domain batch
```

Then start the default database with:

```
asadmin start-database
```

Then create the database `batch` and execute the `derby.ddl.jbatch-tck.sql` DDL script to prepare its structure. 
You can do that with the following steps:

1. Copy the DDL file `com.ibm.jbatch.tck/src/main/resources/ddls/derby.ddl.jbatch-tck.sql` (from the root of the batch-tck source repository)
2. Modify the DDL file to insert a line at the top: `CONNECT 'jdbc:derby://localhost:1527/batch;create=true';`
3. Navigate to the `javadb` directory in the GlassFish installation and execute the following: `bin/ij path/to/the/modified/derby.ddl.jbatch-tck.sql`

Then run the following commands to create the JDBC resource `jdbc/orderDB` batch database.

```
asadmin create-jdbc-connection-pool --datasourceClassname=org.apache.derby.jdbc.ClientDataSource40 --resType=javax.sql.DataSource  --property DatabaseName=batch:serverName=localhost:PortNumber=1527:User=batch:Password=batch batchtck
asadmin create-jdbc-resource --poolName=batchtck jdbc/orderDB
```

Then execute:

```
mvn clean verify -Dit.test=CDITests -Pglassfish-remote
```

If you need, configure Arquillian properties in the `src/test/resources/arquillian.xml` file.

## How to run the TCK against Open Liberty

### prepare Arq env
1. unset WLP_USER_DIR
### Install Open Liberty - (Run tests expecting failure, with the side effect of installing Open Liberty, we can patch it)
2. mvn clean verify   -Dit.test=CDITests -Pliberty-managed  
### Manually patch 
3. cp ..../com.ibm.ws.jbatch.cdi.jakarta_1.0.60.jar  ./target/liberty/wlp/lib   # from building https://github.com/scottkurz/open-liberty/tree/batch21-cdi-fixes  (simple, but not expecting anyone not working on Open Liberty will do this)
### Clean so patch takes effect
4. cd target/liberty/wlp/;  ./bin/server start --clean;  ./bin/server stop
### Now ready to run for real... Don't do 'mvn clean' or you delete the applied patch!
5. cd -; mvn verify -Dit.test=CDITests -Pliberty-managed 

If you need, configure Arquillian properties in the `src/test/resources/arquillian.xml` file, e.g. to attach a debugger to the tests running within the Liberty server.

**NOTE** :  There's nothing special about using the `CDITests.java` file.  We just pick one to simplify and shorten the initial execution so you can see things are set up correctly.  Once setup is confirmed you can run the whole suite.
