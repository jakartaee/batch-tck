/*
 * Copyright 2022 Eclipse Foundation.
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
 */
package ee.jakarta.tck.batch.api.extensions;

import ee.jakarta.tck.batch.api.PropertyKeys;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class VehicleInvocationContextProvider implements TestTemplateInvocationContextProvider {

    Properties props = null;

    public VehicleInvocationContextProvider() {
        props = new Properties(System.getProperties());
        try ( InputStream propertiesInputStream = this.getClass().getClassLoader().getResourceAsStream(PropertyKeys.PROPERTIES_FILE_NAME)) {
            if (propertiesInputStream != null) {
                props.load(propertiesInputStream);
            }
        } catch (IOException ex) {
            Logger.getLogger(VehicleInvocationContextProvider.class.getName()).log(Level.WARNING, ex,
                    () -> "Couldn't find resource " + PropertyKeys.PROPERTIES_FILE_NAME
                    + " on the classpath or read properties from it. Properties from it won't be applied");
        }

    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext arg0) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        List<TestTemplateInvocationContext> invocationContexts = new ArrayList<>();
        Boolean runInVehicles = Boolean.valueOf(props.getProperty(PropertyKeys.RUN_IN_VEHICLES));

        if (runInVehicles) {
            String vehicles = props.getProperty(PropertyKeys.ENABLED_VEHICLES, "").toLowerCase();
            if (vehicles.contains("ejb")) {
                invocationContexts.add(invocationContext("ejb", new EjbExecutionExtension()));
            }
            if (vehicles.contains("web")) {
                invocationContexts.add(invocationContext("web", new WebExecutionExtension()));
            }
            if (vehicles.contains("standalone") || vehicles.isEmpty()) {
                invocationContexts.add(invocationContext("standalone"));
            }
        } else {
            invocationContexts.add(new TestTemplateInvocationContext() {});
        }
        return invocationContexts.stream();
    }

    private TestTemplateInvocationContext invocationContext(String vehicleName) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return getDisplayNameForVehicle(vehicleName);
            }

        };
    }

    private TestTemplateInvocationContext invocationContext(String vehicleName, Extension vehicleExecutionExt) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return getDisplayNameForVehicle(vehicleName);
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Arrays.asList(vehicleExecutionExt);
            }
        };
    }

    private String getDisplayNameForVehicle(String vehicleName) {
        return "[vehicle=" + vehicleName + "]";
    }

}
