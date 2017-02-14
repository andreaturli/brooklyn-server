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
package org.apache.brooklyn.rest.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.brooklyn.rest.domain.Secured;
import org.apache.cxf.rs.security.jose.jwa.SignatureAlgorithm;
import org.apache.cxf.rs.security.jose.jws.JwsJwtCompactConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private PublicKey publicKey;
    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;
    private String realm;
    private String keyPassword;

    @Context
    private ResourceInfo resourceInfo;

    public void init() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        publicKey = keystore.getCertificate(keyAlias).getPublicKey();
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        try {
            final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new NotAuthorizedException("Authorization header must be provided");
            }
            final String token = authorizationHeader.substring("Bearer".length()).trim();
            validateToken(token);
        } catch (Exception e) {
            containerRequestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, String.format("Bearer realm=\"%s\"", realm)).build());
        }

    }

    private void validateToken(final String token) {
        final JwsJwtCompactConsumer consumer = new JwsJwtCompactConsumer(token);
        if (!consumer.verifySignatureWith(publicKey, SignatureAlgorithm.RS256)) {
            throw new NotAuthorizedException("Invalid JWT token");
        }
        List<String> rolesFromToken = (List<String>) consumer.getJwtClaims().getProperty("roles");
        if (!rolesFromToken.containsAll(getRoles(resourceInfo.getResourceMethod()))) {
            throw new NotAuthorizedException("User not authorized");
        }
    }

    private List<String> getRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured != null) {
                return Arrays.asList(secured.roles());
            }
        }
        return new ArrayList<String>();
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
