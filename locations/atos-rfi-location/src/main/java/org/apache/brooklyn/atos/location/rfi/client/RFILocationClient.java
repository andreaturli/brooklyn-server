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
package org.apache.brooklyn.atos.location.rfi.client;

import net.atos.esb.publicschemas.servicerequest.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.GregorianCalendar;

public final class RFILocationClient extends AbstractClient {

    private final ObjectFactory factory;

    public RFILocationClient(String username, String password, String address) {
        super(username, password, address);
        factory = new ObjectFactory();
    }

    @Override
    public Dack open(String orderName) {
        Open open = buildOpen(orderName);
        return this.getPortType().open(open);
    }

    @Override
    public Dack update(Object pack) {
        return this.getPortType().update(buildUpdate());
    }

    @Override
    public Dack processACK(Object pack) {
        // TODO use pack (which will be a Pair<Pack,Update>) to build pack
        return this.getPortType().processACK(buildPack());
    }

    @Override
    public Pack buildPack() {
        Pack pack = factory.createPack();
        HeaderACKType header = factory.createHeaderACKType();
        header.setSrcApplicationID("srcApplicationId");
        header.setMessageID(BigInteger.ONE);
        header.setDstApplicationID("destinationId");

        pack.setHeader(header);
        pack.setReturnCode("0");
        return pack;
    }

    private Open buildOpen(String name) {
        Open open = factory.createOpen();

        HeaderOPNType header = factory.createHeaderOPNType();
        header.setMessageID(BigInteger.valueOf(Long.parseLong(name)));
        header.setSrcApplicationID("COMPOSE_TEST");
        header.setFeedbackMode(FeedbackModeType.ALWAYS);

        CalldataOPNType calldata = factory.createCalldataOPNType();
        calldata.setTicketID("DPCAKV-" + name);
        calldata.setTransactionID("Compose-576a");
        calldata.setShortDescription("DPC SR");
        calldata.setOrganisation("Atos DPC Test Customer 2 FO 1");
        CalldataOPNType.Time time = factory.createCalldataOPNTypeTime();

        GregorianCalendar gcal = new GregorianCalendar();
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException();
        }
        time.setReported(xgcal);
        time.setTimestamp(xgcal);
        calldata.setTime(time);

        OrderDetails orderDetails = factory.createOrderDetails();

        orderDetails.getField().add(createField("hostname", "ampAKlbp" + name));
        orderDetails.getField().add(createField("ou", "ou=DPC_GlobalTemplates,ou=test,ou=LABDEV,dc=LOCAL"));
        orderDetails.getField().add(createField("domain", "LABDEV.LOCAL"));
        orderDetails.getField().add(createField("pass_product", "compose"));
        orderDetails.getField().add(createField("description", "description"));
        orderDetails.getField().add(createField("business_justification", "test"));
        orderDetails.getField().add(createField("vm_owner", "Enduser1_DPC_TC_FO1"));
        orderDetails.getField().add(createField("instance_number", "1"));
        orderDetails.getField().add(createField("target_site", "DPCT2.GB.Beeston.Beeston"));
        orderDetails.getField().add(createField("resource_pool", "DEVCBEECMP001-PL"));
        orderDetails.getField().add(createField("cloud_type", "DDC Cloud Target"));
        orderDetails.getField().add(createField("pod", "BeeDdcPod_TESTFO"));
        orderDetails.getField().add(createField("receiverSysId", "14c84f5537b187009d3b861754990e2b"));
        orderDetails.getField().add(createField("vm_prefix", "DEVWBEEDBCP"));
        orderDetails.getField().add(createField("family", "Balanced Managed"));
        orderDetails.getField().add(createField("managed_type", "managedGeneric"));
        orderDetails.getField().add(createField("network_profile", "Secured Area (SA)"));
        orderDetails.getField().add(createField("server_type", "RHEL 6 Global"));
        orderDetails.getField().add(createField("size", "Medium"));
        orderDetails.getField().add(createField("storage_policy", "DPC-GOLD-SINGLE-COPY"));
        calldata.setOrderDetails(orderDetails);

        PersonType customer = factory.createPersonType();
        customer.setFirstname("Bogdan");
        customer.setLastname("Juszczak");
        customer.setEmail("bogdan.juszczak@innovise-esm.com");
        customer.setOrganisation("Atos DPC Test Customer 2 FO 1");
        customer.setGID("bogdan.juszczak@innovise-esm.com");

        PersonType requester = factory.createPersonType();
        requester.setFirstname("Bogdan");
        requester.setLastname("Juszczak");
        requester.setEmail("bogdan.juszczak@innovise-esm.com");
        requester.setOrganisation("Atos DPC Test Customer 2 FO 1");
        requester.setGID("bogdan.juszczak@innovise-esm.com");

        open.setHeader(header);
        open.setCalldata(calldata);
        open.setCustomer(customer);
        open.setRequester(requester);
        return open;
    }

    public Update buildUpdate() {
        Update update = factory.createUpdate();
        Description description = factory.createDescription();
        description.setValue("hello world");

        update.setDescription(description);

        HeaderUPDType header = factory.createHeaderUPDType();
        header.setSrcApplicationID("srcApplicationID");
        header.setMessageID(BigInteger.ONE);
        header.setDstApplicationID("destinationId");

        update.setHeader(header);
        return update;
    }


}
