package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.Info;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.ui.IntegrationException;

public class InfoEndpoint extends ReadEndpoint<Info> {

    private Info info;

    public InfoEndpoint(Server server) {
        super(server);
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    @Override
    protected Response<Info> handleRequest(Request<?> request, Response<Info> response) throws IntegrationException {
        return response.setBody(info);
    }

    @Override
    protected Class<Info> getResponseClass() {
        return Info.class;
    }

    @Override
    protected String[] getAllowedMethods() {
        return new String[]{ "GET" };
    }

}
