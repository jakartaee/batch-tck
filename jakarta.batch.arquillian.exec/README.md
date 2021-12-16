
# OLD instructions from Ondro

## Requirements

* Clone Batch TCK from https://ondromih@github.com/OndroMih/batch-tck.git to parent directory and switch to the branch poc-junit5

## How it works

1. uses the official usual Arquillian dependencies as for JUnit4, except the JUnit4 runner
2. uses the official Arquillian JUnit5 extension and enables it globally via a service loader file
3. uses an Arquillian extension specific for Batch TCK to create a deployment for each test (the extension is a module of this project)
3. enables the Arquillian extension by setting `junit.jupiter.extensions.autodetection.enabled` system property to true in pom.xml and by creating a service file for the extension class because it doesn't exist in the extension artifact

# How to run the TCK

* start Payara Server at localhost before running the tests
* run the test suite with:

```
mvn clean verify
```

# How to run the TCK against Open Liberty

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
