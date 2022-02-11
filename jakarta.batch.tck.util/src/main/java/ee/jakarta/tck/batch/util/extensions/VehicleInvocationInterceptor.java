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
package ee.jakarta.tck.batch.util.extensions;

import ee.jakarta.tck.batch.util.vehicle.ejb.EjbExecutionInterceptor;
import ee.jakarta.tck.batch.util.vehicle.web.WebExecutionInterceptor;
import ee.jakarta.tck.batch.util.PropertyKeys;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * JUnit5 interceptor that wraps test execution within a vehicle.
 * It's supposed to run within the Jakarta EE container (Arquillian). 
 * It requires that the container adapter (Arquillian extension) adds configuration properties 
 * into a properties file inside the test application. Configuration properties are read from 
 * system properties and the properties file.
 * 
 * If executed outside of container (runInVehicle=false or undefined), it executes tests normally.
 * 
 * If executed inside a container (runInVehicle=true or undefined), it reads the properties to find out 
 * which vehicle to use and delegates to the interceptor that corresponds to that vehicle.
 * 
 * This interceptor executes using only a single vehicle. To execute tests in more vehicles, the tests 
 * need to be executed again, with a different configuration.
 */
public class VehicleInvocationInterceptor implements InvocationInterceptor {

    private final Properties props;

    private final InvocationInterceptor vehicleInterceptor;

    private final Boolean runInVehicle;

    public VehicleInvocationInterceptor() {
        this.props = loadProperties();
        runInVehicle = Boolean.valueOf(props.getProperty(PropertyKeys.RUN_IN_VEHICLES));
        vehicleInterceptor = createVehicleInterceptor();
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        vehicleInterceptor.interceptTestTemplateMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        vehicleInterceptor.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    private Properties loadProperties() {
        Properties props = new Properties(System.getProperties());
        try (InputStream propertiesInputStream = this.getClass().getClassLoader()
                .getResourceAsStream(PropertyKeys.VEHICLE_PROPERTIES_FILE_NAME)) {
            if (propertiesInputStream != null) {
                props.load(propertiesInputStream);
            }
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, ex,
                    () -> "Couldn't find resource " + PropertyKeys.VEHICLE_PROPERTIES_FILE_NAME
                    + " on the classpath or read properties from it. Properties from it won't be applied");
        }
        return props;
    }

    private InvocationInterceptor createVehicleInterceptor() {
        String vehicle = props.getProperty(PropertyKeys.ENABLED_VEHICLE, "").toLowerCase();

        if (runInVehicle) {
            if (vehicle.contains("ejb")) {
                return new EjbExecutionInterceptor(props);
            } else if (vehicle.contains("web")) {
                return new WebExecutionInterceptor();
            }
        }
        return new InvocationInterceptor() {
        };
    }
}
