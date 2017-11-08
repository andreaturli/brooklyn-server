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

import net.atos.esb.publicschemas.servicerequest.*;
import net.atos.esb.publicservices.servicerequest.PortType;

@javax.jws.WebService(serviceName = "Service",
                portName = "PortTypeEndpoint1",
                endpointInterface = "net.atos.esb.publicservices.servicerequest.PortType",
                targetNamespace = "http://esb.atos.net/PublicServices/ServiceRequest")
public class SNOWPortTypeImpl implements PortType {

    @Override
    public Dack update(Update update) {
        Dack dack = new Dack();
        HeaderACKType header = new HeaderACKType();
        HeaderUPDType from = update.getHeader();
        if (from != null) {
            header.setDstApplicationID(from.getSrcApplicationID());
            header.setMessageID(from.getMessageID());
        }
        dack.setHeader(header);
        dack.setReturnMessage("update");
        return dack;
    }

    /**
     * ?xml version="1.0" encoding="UTF-8"?>
     <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
     <SOAP-ENV:Header />
     <SOAP-ENV:Body>
     <ns2:dack xmlns:ns2="http://esb.atos.net/PublicSchemas/ServiceRequest">
     <ns2:header>
     <ns2:messageID>10003009</ns2:messageID>
     <ns2:srcApplicationID>ATOS-SNOWGLOBALDEV-S2</ns2:srcApplicationID>
     </ns2:header>
     <ns2:ticketID>RITM000101442</ns2:ticketID>
     <ns2:extTicketID>DPCAKV-3009</ns2:extTicketID>
     <ns2:returnCode>0</ns2:returnCode>
     <ns2:returnMessage />
     </ns2:dack>
     </SOAP-ENV:Body>
     </SOAP-ENV:Envelope>
     */
    @Override
    public Dack processACK(Pack pack) {
        Dack dack = new Dack();
        dack.setHeader(pack.getHeader());
        dack.setTicketID(pack.getTicketID());
        dack.setReturnCode("0");
        dack.setReturnMessage("OK");
        return dack;
    }

    @Override
    public Dack open(Open part1) {
        Dack dack = new Dack();
        dack.setReturnMessage("open");
        return dack;
    }
}
