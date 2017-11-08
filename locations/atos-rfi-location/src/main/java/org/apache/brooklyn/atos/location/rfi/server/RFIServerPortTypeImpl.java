/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.brooklyn.atos.location.rfi.server;

import net.atos.esb.publicschemas.servicerequest.Dack;
import net.atos.esb.publicschemas.servicerequest.Open;
import net.atos.esb.publicschemas.servicerequest.Pack;
import net.atos.esb.publicschemas.servicerequest.Update;
import net.atos.esb.publicservices.servicerequest.PortType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@javax.jws.WebService(serviceName = "Service",
                portName = "PortTypeEndpoint1",
                endpointInterface = "net.atos.esb.publicservices.servicerequest.PortType",
                targetNamespace = "http://esb.atos.net/PublicServices/ServiceRequest")
public class RFIServerPortTypeImpl implements PortType {

    private BlockingQueue<Object> packBlockingQueue = new LinkedBlockingDeque(1);
    private BlockingQueue<Object> updateBlockingQueue = new LinkedBlockingDeque(1);

    public BlockingQueue<Object> getPackBlockingQueue() {
        return packBlockingQueue;
    }

    public BlockingQueue<Object> getUpdateBlockingQueue() {
        return updateBlockingQueue;
    }

    @Override
    public Dack update(Update update) {
        Dack dack = new Dack();
        dack.setReturnMessage("update");

        updateBlockingQueue.add(update);

        return dack;
    }

    @Override
    public Dack processACK(Pack pack) {
        Dack dack = new Dack();
        dack.setHeader(pack.getHeader());
        dack.setTicketID(pack.getTicketID());
        dack.setReturnCode("0");
        dack.setReturnMessage("OK");

        packBlockingQueue.add(pack);

        return dack;
    }

    @Override
    public Dack open(Open part1) {
        throw new IllegalStateException("open call is not expected on AMP server");
    }

}
