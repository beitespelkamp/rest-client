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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Markus Pauer
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    
    private final Logger LOGGER = Logger.getLogger(WebApplicationExceptionMapper.class.getName());

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        LOGGER.log(Level.FINE, "WebApplicationExceptionMapper Status {0}", status);
        if ((status >= 400 && status < 500) || status > 899) {
            LOGGER.log(Level.FINE, "{0} - {1}", new Object[]{status, exception.getLocalizedMessage()});
        } else {
            LOGGER.log(Level.WARNING, "WebApplicationException: {0}", exception.getLocalizedMessage());
        }
        return exception.getResponse();
    }

}
