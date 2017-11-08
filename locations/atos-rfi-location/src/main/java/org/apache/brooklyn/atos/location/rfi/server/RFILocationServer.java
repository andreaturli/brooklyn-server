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
package org.apache.brooklyn.atos.location.rfi.server;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RFILocationServer extends RFIServer {

    public RFILocationServer(String serverAddress) {
        super(serverAddress, new RFIServerPortTypeImpl());
    }

    public CompletableFuture<Pair<Object, Object>> waitForServiceNowResponses() {
        final RFIServerPortTypeImpl implementor = (RFIServerPortTypeImpl) this.getImplementor();

        // wait for step 2 and 3 asynchronously
        CompletableFuture<Pair<Object, Object>> future = CompletableFuture.supplyAsync(new Supplier<Pair<Object, Object>>() {
            @Override
            public Pair<Object, Object> get() {
                Object pack = null;
                Object update = null;

                BlockingQueue packBlockingQueue = implementor.getPackBlockingQueue();
                BlockingQueue updateBlockingQueue = implementor.getUpdateBlockingQueue();

                try {
                    pack = packBlockingQueue.poll(10, TimeUnit.MINUTES);
                    update = updateBlockingQueue.poll(60, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return ImmutablePair.of(pack, update);
            }
        });

        return future;
    }

}
