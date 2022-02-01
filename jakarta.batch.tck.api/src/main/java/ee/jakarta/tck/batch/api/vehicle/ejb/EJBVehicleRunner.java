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
package ee.jakarta.tck.batch.api.vehicle.ejb;

import ee.jakarta.tck.batch.api.PropertyKeys;
import java.util.Properties;

import ee.jakarta.tck.batch.api.Reporter;
import ee.jakarta.tck.batch.api.extensions.util.TSNamingContext;
import ee.jakarta.tck.batch.api.vehicle.VehicleRunnable;

public class EJBVehicleRunner implements VehicleRunnable {

    @Override
    public void run(Runnable test, Properties p) throws Exception {
        EJBVehicleRemote ref = null;
        String sEJBVehicleJndiName = p.getProperty(PropertyKeys.EJB_VEHICLE_JNDI_NAME, "java:global/jbatch-test-package-all/EJBVehicle");
        TSNamingContext jc = new TSNamingContext();
        ref = jc.lookup(sEJBVehicleJndiName,
                EJBVehicleRemote.class);
        Reporter.logTrace("in ejbvehicle: ref lookup OK; call runTest()");
        ref.runTest(test);
    }
}
