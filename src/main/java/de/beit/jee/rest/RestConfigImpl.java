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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.ejb.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Markus Pauer <mpauer@beit.de>
 */
@Service
@Singleton
public class RestConfigImpl implements RestConfig {
    
    private final Logger logger = LoggerFactory.getLogger(RestConfigImpl.class);
    
    private static final String ENV_VAR = "ENDPOINTS";
    private final Properties props;

    public RestConfigImpl() {
        this.props = new Properties();
        String restConfig = System.getenv(ENV_VAR);
        if (restConfig != null) {
            try {
                this.props.load(new FileInputStream(restConfig));
            } catch (FileNotFoundException ex) {
                logger.error("Die Konfigurationsdatei konnte nicht gefunden werden: {}", ex.getLocalizedMessage());
            } catch (IOException ex) {
                logger.error("Die Konfigurationsdatei konnte nicht gelesen werden: {}", ex.getLocalizedMessage());
            }
        } else {
            logger.error("Die Konfiguration der Endpunkte konnte nicht gefunden werden");
        }
    }

    @Override
    public String getEndpoint(String key) throws ConfigurationException {
        String endpoint = props.getProperty(key + ".endpoint");
        if (endpoint == null) {
            throw new ConfigurationException("Keine Konfiguration für den Endpunkt " + key + " gefunden");
        }
        return endpoint;
    }
    
}
