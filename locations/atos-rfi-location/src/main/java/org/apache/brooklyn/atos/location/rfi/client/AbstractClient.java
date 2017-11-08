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
package org.apache.brooklyn.atos.location.rfi.client;

import net.atos.esb.publicschemas.servicerequest.Dack;
import net.atos.esb.publicschemas.servicerequest.ObjectFactory;
import net.atos.esb.publicschemas.servicerequest.OrderDetails;
import net.atos.esb.publicschemas.servicerequest.Pack;
import net.atos.esb.publicservices.servicerequest.PortType;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public abstract class AbstractClient {

    private final PortType portType;
    private final ObjectFactory factory;

    public AbstractClient(String username, String password, String address) {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(PortType.class);
        jaxWsProxyFactoryBean.setUsername(username);
        jaxWsProxyFactoryBean.setPassword(password);
        jaxWsProxyFactoryBean.setAddress(address);

        jaxWsProxyFactoryBean.getOutInterceptors().add(new LoggingOutInterceptor());
        // add an interceptor to log the incoming response messages
        jaxWsProxyFactoryBean.getInInterceptors().add(new LoggingInInterceptor());
        // add an interceptor to log the incoming fault messages
        jaxWsProxyFactoryBean.getInFaultInterceptors().add(new LoggingInInterceptor());

        portType = (PortType) jaxWsProxyFactoryBean.create();
        factory = new ObjectFactory();
    }

    public PortType getPortType() {
        return portType;
    }

    public abstract Dack open(String orderName);

    public abstract Dack update(Object pack);

    public abstract Dack processACK(Object pack);

    protected OrderDetails.Field createField(String name, String value) {
        OrderDetails.Field field = new OrderDetails.Field();
        field.setName(name);
        field.setValue(value);
        return field;
    }

    public abstract Pack buildPack();

}
