/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.brooklyn.atos.location.rfi;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.apache.brooklyn.api.location.LocationSpec;
import org.apache.brooklyn.core.internal.BrooklynProperties;
import org.apache.brooklyn.core.test.BrooklynMgmtUnitTestSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RFILocationResolverTest extends BrooklynMgmtUnitTestSupport {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(RFILocationResolverTest.class);

    private BrooklynProperties brooklynProperties;

    @BeforeMethod(alwaysRun = true)
    @Override
    public void setUp() throws Exception {
        super.setUp();
        brooklynProperties = mgmt.getBrooklynProperties();

        brooklynProperties.put("brooklyn.location.rfi.identity", "rfi-id");
        brooklynProperties.put("brooklyn.location.rfi.credential", "rfi-cred");
    }

    @Test
    public void testGivesCorrectLocationType() {
        LocationSpec<?> spec = getLocationSpec("rfi");
        assertEquals(spec.getType(), RFILocation.class);

        RFILocation loc = resolve("rfi");
        assertTrue(loc instanceof RFILocation, "loc=" + loc);
    }

    @Test
    public void testParametersInSpecString() {
        RFILocation loc = resolve("rfi(loginUser=myLoginUser,imageId=myImageId)");
//        assertEquals(loc.getConfig(RFILocation.LOGIN_USER), "myLoginUser");
//        assertEquals(loc.getConfig(RFILocation.IMAGE_ID), "myImageId");
    }

    @Test
    public void testTakesDotSeparateProperty() {
        brooklynProperties.put("brooklyn.location.rfi.loginUser", "myLoginUser");
//        RFILocation loc = resolve("rfi");
//        assertEquals(loc.getConfig(RFILocation.LOGIN_USER), "myLoginUser");
    }

    @Test
    public void testPropertiesPrecedence() {
        // prefer those in "spec" over everything else
        brooklynProperties.put("brooklyn.location.named.mydocker", "rfi:(loginUser=\"loginUser-inSpec\")");

        brooklynProperties.put("brooklyn.location.named.mydocker.loginUser", "loginUser-inNamed");
        brooklynProperties.put("brooklyn.location.rfi.loginUser", "loginUser-inDocker");
        brooklynProperties.put("brooklyn.location.jclouds.rfi.loginUser", "loginUser-inJcloudsProviderSpecific");
        brooklynProperties.put("brooklyn.location.jclouds.loginUser", "loginUser-inJcloudsGeneric");

        // prefer those in "named" over everything else
        brooklynProperties.put("brooklyn.location.named.mydocker.privateKeyFile", "privateKeyFile-inNamed");
        brooklynProperties.put("brooklyn.location.rfi.privateKeyFile", "privateKeyFile-inDocker");
        brooklynProperties.put("brooklyn.location.jclouds.rfi.privateKeyFile", "privateKeyFile-inJcloudsProviderSpecific");
        brooklynProperties.put("brooklyn.location.jclouds.privateKeyFile", "privateKeyFile-inJcloudsGeneric");

        // prefer those in rfi-specific
        brooklynProperties.put("brooklyn.location.rfi.publicKeyFile", "publicKeyFile-inDocker");
        brooklynProperties.put("brooklyn.location.jclouds.rfi.publicKeyFile", "publicKeyFile-inJcloudsProviderSpecific");
        brooklynProperties.put("brooklyn.location.jclouds.publicKeyFile", "publicKeyFile-inJcloudsGeneric");

        // prefer those in jclouds provider-specific
        brooklynProperties.put("brooklyn.location.jclouds.rfi.privateKeyPassphrase", "privateKeyPassphrase-inJcloudsProviderSpecific");
        brooklynProperties.put("brooklyn.location.jclouds.privateKeyPassphrase", "privateKeyPassphrase-inJcloudsGeneric");

        // accept those in jclouds generic
        brooklynProperties.put("brooklyn.location.jclouds.privateKeyData", "privateKeyData-inJcloudsGeneric");

        Map<String, Object> conf = resolve("named:mydocker").config().getBag().getAllConfig();

        assertEquals(conf.get("loginUser"), "loginUser-inSpec");
        assertEquals(conf.get("privateKeyFile"), "privateKeyFile-inNamed");
        assertEquals(conf.get("publicKeyFile"), "publicKeyFile-inDocker");
        assertEquals(conf.get("privateKeyPassphrase"), "privateKeyPassphrase-inJcloudsProviderSpecific");
        assertEquals(conf.get("privateKeyData"), "privateKeyData-inJcloudsGeneric");
    }

    private LocationSpec<?> getLocationSpec(String spec) {
        return mgmt.getLocationRegistry().getLocationSpec(spec).get();
    }

    private RFILocation resolve(String spec) {
        return (RFILocation) mgmt.getLocationRegistry().getLocationManaged(spec);
    }
}
