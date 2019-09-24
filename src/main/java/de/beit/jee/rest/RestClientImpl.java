/*
 * Copyright BEIT GmbH and/or licensed to BEIT GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. BEIT licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.beit.jee.rest;

import de.beit.jee.model.User;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Markus Pauer <mpauer@beit.de>
 */
@Service
@Stateless
public class RestClientImpl implements RestClient {
    
    private final Logger logger = LoggerFactory.getLogger(RestClientImpl.class);

    @Autowired
    @Inject
    private RestConfig configuration;

    private ResteasyClient client;
    private KeyStore keyStore;
    private String password;

    public RestClientImpl() {
        InputStream is = null;
        password = System.getProperty("javax.net.ssl.keyStorePassword");
        String keyStorePath = System.getProperty("javax.net.ssl.keyStore");
        if (password != null && keyStorePath != null) {
            try {
                is = new FileInputStream(keyStorePath);
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(is, password.toCharArray());
            } catch (FileNotFoundException ex) {
                logger.error("Could not find keystore {}", ex.getLocalizedMessage());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
                logger.error("", ex.getLocalizedMessage());
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.error("Error while closing the keystore", ex.getLocalizedMessage());
                }
            }
        }
    }

    private ResteasyClient createClient(boolean secured) {
        if (secured) {
            return new ResteasyClientBuilder().keyStore(keyStore, password).build();
        }
        return new ResteasyClientBuilder().build();
    }

    private ResteasyWebTarget getResteasyWebTarget(String url, Class type, boolean secured) {
        client = createClient(secured);
        client.register(ClientLoggingFilter.class);
        ResteasyWebTarget webTarget = (ResteasyWebTarget) client.target(url);
        return webTarget;
    }

    /**
     * 
     * @param <T>
     * @param type
     * @return 
     */
    @Override
    public <T> T endpoint(Class<T> type) {
        try {
            String url = configuration.getEndpoint(type.getName());
            return (T) getResteasyWebTarget(url, type, false).proxy(type);
        } catch (ConfigurationException ex) {
            logger.error("Configuration Exception: {0}", ex.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 
     * @param <T>
     * @param type
     * @return 
     */
    @Override
    public <T> T securedEndpoint(Class<T> type) {
        try {
            String url = configuration.getEndpoint(type.getName());
            return (T) getResteasyWebTarget(url, type, true).proxy(type);
        } catch (ConfigurationException ex) {
            logger.error("Configuration Exception: {0}", ex.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public <T> T endpoint(Class<T> type, User user) {
        try {
            String url = configuration.getEndpoint(type.getName());
            if (user != null) {
                return (T) getResteasyWebTarget(url, type, false).register(new UserHeaderRequestFilter(user)).register(WebApplicationExceptionMapper.class).proxy(type);
            } else {
                return (T) getResteasyWebTarget(url, type, false).register(WebApplicationExceptionMapper.class).proxy(type);
            }
        } catch (ConfigurationException ex) {
            logger.error("Configuration Exception: {0}", ex.getLocalizedMessage());
        }
        return null;
    }
    
}
