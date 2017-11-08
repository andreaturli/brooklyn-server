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

import java.util.Map;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableList;
import net.atos.esb.publicschemas.servicerequest.Dack;
import org.apache.brooklyn.api.location.LocationSpec;
import org.apache.brooklyn.api.location.MachineLocation;
import org.apache.brooklyn.api.location.NoMachinesAvailableException;
import org.apache.brooklyn.atos.location.rfi.client.RFILocationClient;
import org.apache.brooklyn.atos.location.rfi.server.RFILocationServer;
import org.apache.brooklyn.core.entity.BrooklynConfigKeys;
import org.apache.brooklyn.core.location.cloud.AbstractCloudMachineProvisioningLocation;
import org.apache.brooklyn.core.location.cloud.CloudLocationConfig;
import org.apache.brooklyn.location.ssh.SshMachineLocation;
import org.apache.brooklyn.util.core.config.ConfigBag;
import org.apache.brooklyn.util.core.config.ResolvingConfigBag;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.ws.Endpoint;

/**
 * For provisioning rfi items, using the rfi client integration.
 */
public class RFILocation extends AbstractCloudMachineProvisioningLocation {

    @Override
    public MachineLocation obtain(Map<?, ?> flags) throws NoMachinesAvailableException {
        ConfigBag setupRaw = ConfigBag.newInstanceExtending(config().getBag(), flags);
        ConfigBag setup = ResolvingConfigBag.newInstanceExtending(getManagementContext(), setupRaw);

        RFILocationClient snowClient = new RFILocationClient("admin", "password", "http://localhost:9000/snow/rfi");

        String orderName = "12345";
        String rfiLocationAddress = "http://localhost:8081/amp/rfi";

        //  TODO when do we start rfi location server?
        RFILocationServer rfiLocation = new RFILocationServer(rfiLocationAddress);
        Endpoint endpoint = rfiLocation.start();

        // AMP sends an open (step 1)
        Dack dackForStep1 = snowClient.open(orderName);

        //  and wait for step 2 and step 3
        CompletableFuture<Pair<Object, Object>> responses = rfiLocation.waitForServiceNowResponses();
        Pair<Object, Object> result = null;
        try {
            result = responses.get(2l, TimeUnit.HOURS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // stop rfi location server
        endpoint.stop();
        System.out.println(result);

        // TODO use those results to build pack
        Object pack = result.getLeft();
        Object update = result.getRight();
        Dack dackForStep4 = snowClient.processACK(result);
        if (!dackForStep4.getReturnCode().equals("0")) {
            throw new IllegalStateException();
        }

        // TODO ampClient.processAck();
        // TODO create SshMachineLocation
        LocationSpec<SshMachineLocation> locationSpec = buildLocationSpec(setup.get(CALLER_CONTEXT));
        return getManagementContext().getLocationManager().createLocation(locationSpec);
    }

    private LocationSpec<SshMachineLocation> buildLocationSpec(Object callerContext) {
        return LocationSpec.create(SshMachineLocation.class)
                .configure("address", "TODO")
                .configure(SshMachineLocation.PRIVATE_ADDRESSES, ImmutableList.of("TODO"))
                .configure(CloudLocationConfig.USER, "TODO")
                .configure(SshMachineLocation.PASSWORD, "TODO")
                .configure(SshMachineLocation.SSH_PORT, 22) // TODO
                .configure(BrooklynConfigKeys.SKIP_ON_BOX_BASE_DIR_RESOLUTION, true)
                .configure(BrooklynConfigKeys.ONBOX_BASE_DIR, "/tmp")
                .configure(CALLER_CONTEXT, callerContext);
    }

    @Override
    public void release(MachineLocation machine) {

    }

}
