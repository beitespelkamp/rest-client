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
package de.beit.jee.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

/**
 * This Configuration Class gives you access to the central Property-Files.
 * <p>
 * To use this Configuration simply inject this Singleton into the Enterprise
 * Java Bean. If a property file has changed, it will be automatically
 * reloaded.</p>
 *
 * @author Markus Pauer
 */
@Singleton
@ApplicationScoped
public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private static final String SECRETS = "SECRETS";
    private static final String CONFIG = "CONFIG";

    private final Properties configuration;
    private final Properties secrets;
    private String basePath;

    public Configuration() {
        this.configuration = new Properties();
        this.secrets = new Properties();
    }

    @PostConstruct
    private final void init() {
        File configFile = new File(System.getenv(CONFIG));
        basePath = configFile.getParent();
        try {
            configuration.load(new FileReader(configFile));
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Config-File not found: {0}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Config-File not loaded: {0}", ex.getLocalizedMessage());
        }
        LOGGER.log(Level.FINE, "Config-File loaded: {0} entries found", configuration.size());

        File passwordFile = new File(System.getenv(SECRETS));
        try {
            secrets.load(new FileReader(passwordFile));
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Password-File not found: {0}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Password-File not loaded: {0}", ex.getLocalizedMessage());
        }
        LOGGER.log(Level.FINE, "Password-File loaded: {0} entries found", configuration.size());
    }

    /**
     * Get the Property with the given key.
     *
     * @param name - key of the Property
     * @return value of the Property
     * @throws PropertyNotFoundException
     */
    public String getProperty(String name) throws PropertyNotFoundException {
        String property;
        property = configuration.getProperty(name);
        if (property == null) {
            property = secrets.getProperty(name);
            if (property == null) {
                throw new PropertyNotFoundException(name, false);
            }
        }
        return property;
    }

    /**
     * Read the Content of a JSON-based Property-File completely.
     *
     * @param name - name of the Property-File
     * @return JSON-structure
     * @throws PropertyNotFoundException
     */
    public String getContent(String name) throws PropertyNotFoundException {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(basePath + "/" + name + ".json"));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Fehler beim Holen der Datei: {0}", ex.getLocalizedMessage());
            throw new PropertyNotFoundException(name + ".json", true);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Konnte die Datei {0} nicht lesen", name + ".json");
        }
        return content.toString();
    }

}
