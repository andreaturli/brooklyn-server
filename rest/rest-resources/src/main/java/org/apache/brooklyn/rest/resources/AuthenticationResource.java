package org.apache.brooklyn.rest.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.brooklyn.rest.api.AuthenticationApi;
import org.apache.brooklyn.rest.domain.Credential;
import org.apache.brooklyn.rest.domain.TokenHolder;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.rs.security.jose.common.JoseType;
import org.apache.cxf.rs.security.jose.jwa.SignatureAlgorithm;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.JwsJwtCompactProducer;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/auth-api")
public class AuthenticationResource extends AbstractBrooklynRestResource implements AuthenticationApi {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

    private String jaasRealm;
    private String jwtIssuer;
    private Long jwtTimeToLive;
    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;
    private PrivateKey privateKey;

    public void init() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        privateKey = (PrivateKey) keystore.getKey(keyAlias, keyPassword.toCharArray());
    }

    private TokenHolder loginAndIssueToken(final String username, final String password) {
        LOG.debug("Authenticating user [{}]", username);

        try {
            LoginContext loginContext = new LoginContext(jaasRealm, new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (final Callback callback : callbacks) {
                        if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        } else if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(username);
                        }
                    }
                }
            });
            loginContext.login();
            LOG.info("Authenticated user [{}] ... issuing token", username);
            Set<String> roles = new TreeSet<String>();
            Set<String> groups = new TreeSet<String>();

            for (final Principal principal : loginContext.getSubject().getPrincipals()) {
                if (principal instanceof RolePrincipal) {
                    roles.add(principal.getName());
                } else if (principal instanceof GroupPrincipal) {
                    groups.add(principal.getName());
                }
            }

            JwsHeaders headers = new JwsHeaders(JoseType.JWT, SignatureAlgorithm.RS256);
            JwtClaims claims = new JwtClaims();
            claims.setSubject(username);
            claims.setIssuer(jwtIssuer);
            claims.setAudience(InetAddress.getLocalHost().getHostAddress());
            claims.setTokenId(UUID.randomUUID().toString());

            long issuedAt = OAuthUtils.getIssuedAt();
            claims.setIssuedAt(issuedAt);
            claims.setExpiryTime(issuedAt + jwtTimeToLive);
            claims.setProperty("roles", roles);
            claims.setProperty("groups", groups);
            JwtToken token = new JwtToken(headers, claims);
            JwsJwtCompactProducer tokenProducer = new JwsJwtCompactProducer(token);
            return new TokenHolder(tokenProducer.signWith(privateKey), "Bearer");
        } catch (Exception e) {
            throw new AuthenticationException(String.format("Could not authenticate user [%s]", username));
        }
    }

    public String getJaasRealm() {
        return jaasRealm;
    }

    public void setJaasRealm(String jaasRealm) {
        this.jaasRealm = jaasRealm;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public Long getJwtTimeToLive() {
        return jwtTimeToLive;
    }

    public void setJwtTimeToLive(Long jwtTimeToLive) {
        this.jwtTimeToLive = jwtTimeToLive;
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

    @Override
    public Response formLogin(@FormParam("username") String username, @FormParam("password") String password) {
        return Response.ok(loginAndIssueToken(username, password)).build();
    }

    @Override
    public Response authenticateUser(Credential credential) {
        return Response.ok(loginAndIssueToken(credential.getUsername(), credential.getPassword())).build();
    }
}
