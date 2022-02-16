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
package ee.jakarta.tck.batch.util.vehicle.ejb;

import ee.jakarta.tck.batch.util.Reporter;
import java.lang.reflect.Method;
import java.util.Properties;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

public class EjbExecutionInterceptor implements InvocationInterceptor {

    private Properties props;
    
    public EjbExecutionInterceptor(Properties props) {
        this.props = props;
    }
    
    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        Reporter.logTrace("Invoking test method " + invocationContext.getExecutable().getName() + " in Enterprise Bean.");
        new EJBVehicleRunner().run(() -> {
            try {
                invocation.proceed();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }, props);
        Reporter.logTrace("...finished test method " + invocationContext.getExecutable().getName() + " in Enterprise Bean.");
    }
}
