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

import com.google.common.collect.Maps;
import net.atos.esb.publicschemas.servicerequest.Dack;
import org.apache.brooklyn.atos.location.rfi.client.RFILocationClient;
import org.apache.brooklyn.atos.location.rfi.server.ServiceNow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.ws.Endpoint;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * End-to-end test using mock ServiceNow and mock AMP
 */
public class RFILocationMockTest {

    private static final Logger LOG = LoggerFactory.getLogger(RFILocationMockTest.class);

    private static final String SNOW_ENDPOINT = System.getProperty("test.rfi.amp.endpoint", "http://localhost:9000/snow/rfi");
    private static final String AMP_USERNAME = System.getProperty("test.rfi.amp.identity", "admin");
    private static final String AMP_PASSWORD = System.getProperty("test.rfi.amp.credential", "password");

    protected AMP amp;
    protected Endpoint serviceNowEndpoint;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        serviceNowEndpoint = new ServiceNow("http://localhost:9000/snow/rfi").start();

        String username = "admin";
        String password = "password";
        String address = "http://localhost:8081/amp/rfi";
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                RFILocationClient rfiLocationClient = new RFILocationClient(username, password, address);

                // step 2 - snow sends a processAck
                Dack dackForStep2 = rfiLocationClient.processACK(null); // TODO

                // step 3 - snow sends a openUpdate
                Dack dackForStep3 = rfiLocationClient.update(null); // TODO
            }
        }, 1L, TimeUnit.SECONDS);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        serviceNowEndpoint.stop();
    }

    @Test()
    public void testEndToEnd() throws Exception {

        amp = new AMP(AMP_USERNAME, AMP_PASSWORD, SNOW_ENDPOINT);
        amp.obtain(Maps.newConcurrentMap());

    }
}
