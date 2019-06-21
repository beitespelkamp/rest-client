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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

/**
 *
 * @author Markus Pauer
 */
public class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter {
    
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Logger.getLogger(ClientLoggingFilter.class.getName()).log(Level.FINE, "ClientRequest {0} mit Methode {1}", new Object[]{requestContext.getUri(), requestContext.getMethod()});
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Logger.getLogger(ClientLoggingFilter.class.getName()).log(Level.FINE, "ClientResponse {0}", responseContext.getStatus());
    }

}
