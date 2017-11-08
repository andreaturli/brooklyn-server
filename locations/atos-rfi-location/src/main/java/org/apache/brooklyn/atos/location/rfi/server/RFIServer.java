/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.brooklyn.atos.location.rfi.server;

import net.atos.esb.publicservices.servicerequest.PortType;

import javax.xml.ws.Endpoint;

public abstract class RFIServer {

    private final String address;
    private final PortType implementor;

    public String getAddress() {
        return address;
    }

    public PortType getImplementor() {
        return implementor;
    }


    public RFIServer(String address, PortType implementor) {
        this.address = address;
        this.implementor = implementor;
    }

    public Endpoint start() {
        Endpoint endpoint = Endpoint.publish(address, implementor);
        System.out.println("Server " + address + " ready...");
        return endpoint;
    }

    public void stop(Endpoint endpoint) {
        endpoint.stop();
    }

}