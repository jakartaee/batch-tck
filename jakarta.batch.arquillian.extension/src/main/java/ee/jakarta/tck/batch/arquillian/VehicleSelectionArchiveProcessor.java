/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ee.jakarta.tck.batch.arquillian;

import ee.jakarta.tck.batch.util.PropertyKeys;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;

/**
 * Copies the system properties related to execution in test vehicles and 
 * adds them into a properties file inside the test deployment package.
 * 
 * The test vehicle (JUnit) extension can then read the properties file from the classpath.
 * 
 * @author omihalyi
 */
public class VehicleSelectionArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> archive, TestClass arg1) {
        Properties props = new Properties();
        String enabledVehicles = System.getProperty(PropertyKeys.ENABLED_VEHICLE);
        // if we have enabled vehicles, pass them and enable the vehicles feature. Otherwise all runs with defaults.
        if (enabledVehicles != null) {
            props.put(PropertyKeys.ENABLED_VEHICLE, enabledVehicles);
            props.put(PropertyKeys.RUN_IN_VEHICLES, Boolean.TRUE.toString());
        }
        String propertiesContent = propertiesToStringContent(props);
        DeploymentPackageType deploymentpackage
                = DeploymentPackageType.fromArchive(archive);
        deploymentpackage.getPackageBuilder(archive)
                // store properties into a properties file
                .addResource(new StringAsset(propertiesContent), PropertyKeys.PROPERTIES_FILE_NAME)
                .build();
    }

    private String propertiesToStringContent(Properties props) {
        try ( StringWriter propertiesWriter = new StringWriter()) {
            props.store(propertiesWriter, null);
            return propertiesWriter.toString();
        } catch (IOException ex) { // can't happen from StringWriter
            throw new RuntimeException(ex);
        }
    }

}
