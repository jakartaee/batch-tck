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
# Instructions how to run the TCK using this module

## How it works

1. Uses the standard Arquillian dependencies as for JUnit4, except the JUnit4 runner
2. Uses the official Arquillian JUnit5 extension and enables it globally via a service loader file
3. Enables the Arquillian JUnit5 extension by setting `junit.jupiter.extensions.autodetection.enabled` system property to true in pom.xml
4. Uses an Arquillian extension specific for Batch TCK to create a deployment for each test 
5. Generates the `test.properties` file with default test properties, which can be later modified and versioned. Existing `test.properties` file won't be overwritten.

IMPORTANT: The properties from the `test.properties` file are then added to the maven execution but note that they are not passed to the Arquillian container. 
If you want to apply them within the implementation container, you need to apply them to the implementation in a vendor-specific way, before you execute the TCK.

The Batch TCK Arquillian extension is a separate module. It contains 

* The Arquillian extension class
* A service loader file to register the extension with Arquillian
* A service loader file to register the Arquillian JUnit 5 extension with JUnit 5 (because it's not included in the extension module)

# How to run the TCK

Create a new maven project that:

* Uses this maven artifact as the parent
* Contains the Arquillian container for the target runtime, including all its dependencies and configuration for it
* (Required) Contains the `maven-dependency-plugin` in the list of plugins. This plugin 
is pre-configured in the parent POM to copy test resources from the TCK artifact
* (Optional) Specify to exclude some artifacts on the maven test classpath from the Arquillian test 
deployment with the `arquillian.extensions.jakarta.batch.groupPrefixesToIgnore` system property if they cause problems. 
Specify prefixes of group names, separated by a column.
* (Optional) Contains the `xml-maven-plugin` and `echo-maven-plugin` in the list of plugins in the respected order 
(first the xml plugin, then echo plugin). These plugins will transfor the summary from the failsafe plugin and print it to output. 
This can output can be used to report test results for Jakarta TCK certification.
* (Optional) Execute `mvn pre-integration-test` and then apply the generated `test.properties` to the configuration of 
the implementation under the tests. Note that, even though these properties are applied for test execution, they are not transferred 
into the Arquillian container and they have to be applied in a way that is specific to the implementation. For example, in GlassFish, 
you can apply each property using `asadmin create-system-properties` against a running GlassFish server
* (Optional) If the JNDI name of the `EJBVehicleRemote` EJB is different from the default name, specify the correct name using 
the `jakarta.batch.tck.vehicles.ejb.jndiName` system property, either in the failsafe maven plugin, or inside the implementation container.

An example for GlassFish:

```
    <parent>
        <groupId>jakarta.batch</groupId>
        <artifactId>jakarta.batch.arquillian.exec-parent</artifactId>
        <version>2.1.0-M2-SNAPSHOT</version>
    </parent>

    <artifactId>glassfish-batch-tck-execution</artifactId>
    <packaging>pom</packaging>
    <name>Jakarta Batch GlassFish Execution</name>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <configuration>
                    <systemPropertyVariables>
                        <arquillian.extensions.jakarta.batch.groupPrefixesToIgnore>org.glassfish</arquillian.extensions.jakarta.batch.groupPrefixesToIgnore>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>xml-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>com.github.ekryd.echo-maven-plugin</groupId>
                <artifactId>echo-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.jboss.arquillian.container</groupId>
            <artifactId>arquillian-glassfish-remote-6</artifactId>
            <version>1.0.0.Alpha1</version>
        <dependency>
    </dependencies>
```

For a complete example, see the example project in the `jakarta.batch.arquillian.exec` directory in the Batch TCK sources.

Then run the test suite with the following executed in the new project:

```
mvn clean verify
```

