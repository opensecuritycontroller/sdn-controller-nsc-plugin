package com.intelsecurity.isc.controller.api;

import javax.ws.rs.core.MediaType;

import com.mcafee.vmidc.rest.client.RestBaseClient;

class NeutronSecurityControllerRestClient extends RestBaseClient {

    private static final String BASE_URL = "/v1.0/nsc/";
    private static int PORT = 5555;

    public NeutronSecurityControllerRestClient(String ipAddress, String loginName, String password) {
        super(BASE_URL, MediaType.APPLICATION_JSON);
        initRestBaseClient(ipAddress, PORT, loginName, password, false);
    }

}
