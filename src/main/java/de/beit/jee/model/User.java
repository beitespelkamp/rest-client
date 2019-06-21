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
package de.beit.jee.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Markus Pauer
 */
@XmlRootElement
public class User implements Serializable {

    private Long userId;
    private String name;
    private final List<String> roles;
    private final List<String> customer;

    public User() {
        this.roles = new ArrayList<>();
        this.customer = new ArrayList<>();
    }

    @JsonIgnore
    public String encodeUser() {
        try {
            byte[] encodedByteArray = Base64.encodeBase64(new ObjectMapper().writeValueAsString(this).getBytes("UTF-8"));
            return new String(encodedByteArray, "UTF-8");
        } catch (JsonProcessingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static User decodeUser(String encodedUser) {
        try {
            byte[] decodedBytes = Base64.decodeBase64(encodedUser);
            return new ObjectMapper().readValue(new String(decodedBytes, "UTF-8"), User.class);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public boolean hasRole(String roleName) {
        for (String role : roles) {
            if (role.equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getCustomer() {
        return customer;
    }

    public void addCustomer(String customerNr) {
        this.customer.add(customerNr);
    }

    public boolean hasCustomer(String customerNr) {
        for (String customer : customer) {
            if (customer.equals(customerNr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", name=" + name + ", roles=" + roles + ", customer=" + customer + '}';
    }

}
