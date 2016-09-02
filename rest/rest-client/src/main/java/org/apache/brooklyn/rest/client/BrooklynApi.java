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
package org.apache.brooklyn.rest.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.brooklyn.rest.api.AccessApi;
import org.apache.brooklyn.rest.api.ActivityApi;
import org.apache.brooklyn.rest.api.ApplicationApi;
import org.apache.brooklyn.rest.api.CatalogApi;
import org.apache.brooklyn.rest.api.EffectorApi;
import org.apache.brooklyn.rest.api.EntityApi;
import org.apache.brooklyn.rest.api.EntityConfigApi;
import org.apache.brooklyn.rest.api.LocationApi;
import org.apache.brooklyn.rest.api.PolicyApi;
import org.apache.brooklyn.rest.api.PolicyConfigApi;
import org.apache.brooklyn.rest.api.ScriptApi;
import org.apache.brooklyn.rest.api.SensorApi;
import org.apache.brooklyn.rest.api.ServerApi;
import org.apache.brooklyn.rest.api.UsageApi;
import org.apache.brooklyn.rest.api.VersionApi;
import org.apache.brooklyn.util.net.Urls;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("deprecation")
public class BrooklynApi {

    private final String target;
    private final int maxPoolSize;
    private final int timeOutInMillis;
    private final Credentials credentials;

    private static final Logger LOG = LoggerFactory.getLogger(BrooklynApi.class);
    private final List<?> providers;

    /**
     * @deprecated since 0.9.0. Use {@link BrooklynApi#newInstance(String)} instead
     */
    @Deprecated
    public BrooklynApi(URL endpoint) {
        this(checkNotNull(endpoint, "endpoint").toString());
    }

    /**
     * @deprecated since 0.9.0. Use {@link BrooklynApi#newInstance(String)} instead
     */
    @Deprecated
    public BrooklynApi(String endpoint) {
        // username/password cannot be null, but credentials can
        this(endpoint, null);
    }

    /**
     * @deprecated since 0.9.0. Use {@link BrooklynApi#newInstance(String, String, String)} instead
     */
    @Deprecated
    public BrooklynApi(URL endpoint, String username, String password) {
        this(endpoint.toString(), new UsernamePasswordCredentials(username, password));
    }

    /**
     * @deprecated since 0.9.0. Use {@link BrooklynApi#newInstance(String, String, String)} instead
     */
    @Deprecated
    public BrooklynApi(String endpoint, String username, String password) {
        this(endpoint, new UsernamePasswordCredentials(username, password));
    }

    /**
     * @deprecated since 0.9.0. Use {@link BrooklynApi#newInstance(String, String, String)} instead
     */
    @Deprecated
    public BrooklynApi(URL endpoint, @Nullable Credentials credentials) {
        this(endpoint.toString(), credentials);
    }

    /**
     * Creates a BrooklynApi using an HTTP connection pool
     *
     * @deprecated since 0.9.0. Use {@link BrooklynApi#newInstance(String, String, String)} instead
     */
    @Deprecated
    public BrooklynApi(String endpoint, @Nullable Credentials credentials) {
        this(endpoint, credentials, 20, 5000, ImmutableList.of(new JacksonJsonProvider()));
    }

    /**
     * Creates a BrooklynApi using an HTTP connection pool
     *
     * @param endpoint the Brooklyn endpoint
     * @param credentials user credentials or null
     * @param maxPoolSize maximum pool size
     * @param timeOutInMillis connection timeout in milliseconds
     */
    public BrooklynApi(String endpoint, @Nullable Credentials credentials, int maxPoolSize, int timeOutInMillis, List<?> providers) {
        try {
            new URL(checkNotNull(endpoint, "endpoint"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        this.target = addV1SuffixIfNeeded(endpoint);
        this.maxPoolSize = maxPoolSize;
        this.timeOutInMillis = timeOutInMillis;
        this.credentials = credentials;
        this.providers = providers;
    }

    private String addV1SuffixIfNeeded(String endpoint) {
        if (!endpoint.endsWith("/v1/") && !endpoint.endsWith("/v1")) {
            return Urls.mergePaths(endpoint, "v1");
        } else {
            return endpoint;
        }
    }

    private Supplier<PoolingHttpClientConnectionManager> connectionManagerSupplier = Suppliers.memoize(new Supplier<PoolingHttpClientConnectionManager>() {
        @Override
        public PoolingHttpClientConnectionManager get() {
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(maxPoolSize);
            connManager.setDefaultMaxPerRoute(maxPoolSize);
            return connManager;
        }
    });

    private Supplier<RequestConfig> reqConfSupplier = Suppliers.memoize(new Supplier<RequestConfig>() {
        @Override
        public RequestConfig get() {
            return RequestConfig.custom()
                    .setConnectTimeout(timeOutInMillis)
                    .setConnectionRequestTimeout(timeOutInMillis)
                    .build();
        }
    });

    /**
     * Creates a BrooklynApi using an HTTP connection pool
     *
     * @param endpoint the Brooklyn endpoint
     * @return a new BrooklynApi instance
     */
    public static BrooklynApi newInstance(String endpoint) {
        return new BrooklynApi(endpoint, null);
    }

    /**
     * Creates a BrooklynApi using an HTTP connection pool
     *
     * @param endpoint the Brooklyn endpoint
     * @param username for authentication
     * @param password for authentication
     * @return a new BrooklynApi instance
     */
    public static BrooklynApi newInstance(String endpoint, String username, String password) {
        return new BrooklynApi(endpoint, new UsernamePasswordCredentials(username, password));
    }

    public ActivityApi getActivityApi() {
        return JAXRSClientFactory.create(target, ActivityApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public ApplicationApi getApplicationApi() {
        return JAXRSClientFactory.create(target, ApplicationApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public CatalogApi getCatalogApi() {
        return JAXRSClientFactory.create(target, CatalogApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public EffectorApi getEffectorApi() {
        return JAXRSClientFactory.create(target, EffectorApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public EntityConfigApi getEntityConfigApi() {
        return JAXRSClientFactory.create(target, EntityConfigApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public EntityApi getEntityApi() {
        return JAXRSClientFactory.create(target, EntityApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public LocationApi getLocationApi() {
        return JAXRSClientFactory.create(target, LocationApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public PolicyConfigApi getPolicyConfigApi() {
        return JAXRSClientFactory.create(target, PolicyConfigApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public PolicyApi getPolicyApi() {
        return JAXRSClientFactory.create(target, PolicyApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public ScriptApi getScriptApi() {
        return JAXRSClientFactory.create(target, ScriptApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public SensorApi getSensorApi() {
        return JAXRSClientFactory.create(target, SensorApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public ServerApi getServerApi() {
        return JAXRSClientFactory.create(target, ServerApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public UsageApi getUsageApi() {
        return JAXRSClientFactory.create(target, UsageApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public VersionApi getVersionApi() {
        return JAXRSClientFactory.create(target, VersionApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }

    public AccessApi getAccessApi() {
        return JAXRSClientFactory.create(target, AccessApi.class, providers, credentials.getUserPrincipal().getName(), credentials.getPassword(), null);
    }
/*
    public static <T> T getEntity(Response response, Class<T> type) {
        if (response instanceof ResponseImpl) {
            ResponseImpl clientResponse = (ResponseImpl) response;
            return clientResponse.getEntity(type);
        } else if (response instanceof BuiltResponse) {
            // Handle BuiltResponsePreservingError turning objects into Strings
            if (response.getEntity() instanceof String && !type.equals(String.class)) {
                return new Gson().fromJson(response.getEntity().toString(), type);
            }
        }
        // Last-gasp attempt.
        return type.cast(response.getEntity());
    }

    public static <T> T getEntity(Response response, GenericType<T> type) {
        if (response instanceof ResponseImpl) {
            ResponseImpl clientResponse = (ResponseImpl) response;
            return clientResponse.getEntity(type);
        } else if (response instanceof BuiltResponse) {
            // Handle BuiltResponsePreservingError turning objects into Strings
            if (response.getEntity() instanceof String) {
                return new Gson().fromJson(response.getEntity().toString(), type.getGenericType());
            }
        }
        // Last-gasp attempt.
        return type.getType().cast(response.getEntity());
    }
*/

}
