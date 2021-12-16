

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