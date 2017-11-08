/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.brooklyn.atos.location.rfi;

import io.cloudsoft.brooklyn.jaxws.plugin.client.RFILocationClient;
import io.cloudsoft.brooklyn.jaxws.plugin.server.RFILocationServer;
import net.atos.esb.publicschemas.servicerequest.Dack;
import org.apache.brooklyn.api.location.MachineLocation;
import org.apache.brooklyn.api.location.NoMachinesAvailableException;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.ws.Endpoint;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AMP {

    private final RFILocationClient snowClient;

    public AMP(String username, String password, String address) {
        this.snowClient = new RFILocationClient(username, password, address);

    }
    public AMP(RFILocationClient snowClient) {
        this.snowClient = snowClient;
    }

    public MachineLocation obtain(Map<?, ?> flags) throws NoMachinesAvailableException {

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

        // TODO create SshMachineLocation, presumably using result
        return null;

    }

}
