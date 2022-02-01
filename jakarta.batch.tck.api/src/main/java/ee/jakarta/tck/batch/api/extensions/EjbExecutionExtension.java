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

import ee.jakarta.tck.batch.api.Reporter;
import ee.jakarta.tck.batch.api.vehicle.ejb.EJBVehicleRunner;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

public class EjbExecutionExtension implements InvocationInterceptor {

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
        Reporter.logTrace("Invoking test method " + invocationContext.getExecutable().getName() + " in EJB...");
        new EJBVehicleRunner().run(() -> {
            try {
                invocation.proceed();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }, System.getProperties());
        Reporter.logTrace("...finished test method " + invocationContext.getExecutable().getName() + " in EJB.");
    }
}
