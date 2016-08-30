/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.oe.api.restapi;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.oe.OrchestrationEngineException;
import com.emc.storageos.services.restutil.StandardRestClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 * This class implements methods for calling OE REST API to do operations.
 */

public class OrchestrationEngineRestClient extends StandardRestClient {

    private static Logger log = LoggerFactory.getLogger(OrchestrationEngineRestClient.class);
    private static final String API_LOGIN = "/login";

    /**
     * Constructor
     * 
     * @param factory A reference to the OrchestrationRestClientFactory
     * @param baseURI the base URI to connect to OE Gateway
     * @param client A reference to a Jersey Apache HTTP client.
     * @param username The OE username.
     * @param password The OE user password.
     */
    public OrchestrationEngineRestClient(URI baseURI, String username, String password, Client client) {
        _client = client;
        _base = baseURI;
        _username = username;
        _password = password;
        _authToken = "";
    }

    public void setUsername(String username) {
        _username = username;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public String init() throws Exception {
        return null; //getVersion();
    }

    @Override
    protected Builder setResourceHeaders(WebResource resource) {
        return resource.getRequestBuilder();
    }

    @Override
    protected void authenticate() {
        _client.removeAllFilters();
        _client.addFilter(new HTTPBasicAuthFilter(_username, _password));
        if (log.isDebugEnabled()) {
            _client.addFilter(new LoggingFilter(System.out));
        }
        URI requestURI = _base.resolve(URI.create(API_LOGIN));
        ClientResponse response = _client.resource(requestURI).type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        if (response.getClientResponseStatus() != ClientResponse.Status.OK
                && response.getClientResponseStatus() != ClientResponse.Status.CREATED) {
            throw OrchestrationEngineException.exceptions.authenticationFailure(_base.toString());
        }
        _authToken = response.getEntity(String.class).replace("\"", "");
        _client.removeAllFilters();
        _client.addFilter(new HTTPBasicAuthFilter(_username, _authToken));
        if (log.isDebugEnabled()) {
            _client.addFilter(new LoggingFilter(System.out));
        }
    }

    @Override
    protected int checkResponse(URI uri, ClientResponse response) { 
        ClientResponse.Status status = response.getClientResponseStatus();
        int errorCode = status.getStatusCode();
        if (errorCode >= 300) {
            String entity = null;
            try {
                entity = response.getEntity(String.class);
            } catch (Exception e) {   
                log.error("Parsing the failure response object failed.  " +
                        e.getMessage() + " where entity was " + 
                        entity ,e);
            }
            if (errorCode == 404 || errorCode == 410) {
                throw OrchestrationEngineException.exceptions.resourceNotFound(uri.toString());
            } else if (errorCode == 401) {
                throw OrchestrationEngineException.exceptions.authenticationFailure(uri.toString());
            } else {
                throw OrchestrationEngineException.exceptions.internalError(uri.toString(), entity);
            }
        } else {
            return errorCode;
        }
    }
}