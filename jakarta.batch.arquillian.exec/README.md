<!--- 
Copyright (c) 2021 Contributors to the Eclipse Foundation

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

# An example how to run the TCK with the Arquillian execution module

## How to run the TCK against GlassFish

NOTE: The TCK doesn't pass with any existing GlassFish release yet. 
The work on GlassFish 7.x has only started and support for Batch 2.1 has not been implemented there yet.

First, install GlassFish on the same machine and then start it with the default configuration. (`asadmin start-domain`).

Then start the default database with `asadmin start-database`).

Then execute:

```
mvn clean verify -Dit.test=CDITests -Pglassfish-remote
```

If you need, configure Arquillian properties in the `src/test/resources/arquillian.xml` file.

## How to run the TCK against Open Liberty

### prepare Arq env
1. unset WLP_USER_DIR
### Install Liberty
2. mvn clean verify   -Dit.test=CDITests -Pliberty-managed  
### Manually patch 
3. cp ..../com.ibm.ws.jbatch.cdi.jakarta_1.0.60.jar  ./target/liberty/wlp/lib   # from building https://github.com/scottkurz/open-liberty/tree/batch21-cdi-fixes  (simple, but not expecting anyone not working on Open Liberty will do this)
### Clean so patch takes effect
4. cd target/liberty/wlp/;  ./bin/server start --clean;  ./bin/server stop
### Now ready to run for real... Don't clean or you delete the applied patch!
5. cd -; mvn verify   -Dit.test=CDITests -Pliberty-managed 
