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

import de.beit.jee.config.Configuration;
import de.beit.jee.config.PropertyNotFoundException;
import de.beit.jee.model.User;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * This Client ...
 *
 * @author Markus Pauer
 */
@Named
@Stateless
public class RestClient implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(RestClient.class.getName());

    @Inject
    private Configuration configuration;

    private ResteasyClient client;
    private KeyStore keyStore;
    private String password;

    public RestClient() {
        InputStream is = null;
        password = System.getProperty("javax.net.ssl.keyStorePassword");
        String keyStorePath = System.getProperty("javax.net.ssl.keyStore");
        if (password != null && keyStorePath != null) {
            try {
                is = new FileInputStream(keyStorePath);
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(is, password.toCharArray());
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
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
        for (String key : client.getConfiguration().getProperties().keySet()) {
            Logger.getLogger(RestClient.class.getName()).log(Level.INFO, "{0}: {1}", new Object[]{key, client.getConfiguration().getProperty(key)});
        }
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
    public <T> T endpoint(Class<T> type) {
        try {
            String url = configuration.getProperty(type.getName() + ".endpoint");
            return (T) getResteasyWebTarget(url, type, false).proxy(type);
        } catch (PropertyNotFoundException ex) {
            Logger.getLogger(RestClient.class.getName()).log(Level.SEVERE, "Configuration Exception: {0}", ex.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 
     * @param <T>
     * @param type
     * @return 
     */
    public <T> T securedEndpoint(Class<T> type) {
        try {
            String url = configuration.getProperty(type.getName() + ".endpoint");
            return (T) getResteasyWebTarget(url, type, true).proxy(type);
        } catch (PropertyNotFoundException ex) {
            Logger.getLogger(RestClient.class.getName()).log(Level.SEVERE, "Configuration Exception: {0}", ex.getLocalizedMessage());
        }
        return null;
    }

    public <T> T endpoint(Class<T> type, User user) {
        try {
            String url = configuration.getProperty(type.getName() + ".endpoint");
            if (user != null) {
                return (T) getResteasyWebTarget(url, type, false).register(new UserHeaderRequestFilter(user)).register(WebApplicationExceptionMapper.class).proxy(type);
            } else {
                return (T) getResteasyWebTarget(url, type, false).register(WebApplicationExceptionMapper.class).proxy(type);
            }
        } catch (PropertyNotFoundException ex) {
            Logger.getLogger(RestClient.class.getName()).log(Level.SEVERE, "Configuration Exception: {0}", ex.getLocalizedMessage());
        }
        return null;
    }

}
