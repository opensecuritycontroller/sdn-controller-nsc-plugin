/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc.restserver;

import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.*;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osc.controller.nsc.restserver.api.InspectionHookApis;
import org.osc.controller.nsc.restserver.api.InspectionPortApis;
import org.osc.controller.nsc.restserver.api.NetworkElementApis;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Component(name = "nsc.servlet", service = Servlet.class, property = {

        HTTP_WHITEBOARD_SERVLET_NAME + "=" + "NSC-API", HTTP_WHITEBOARD_SERVLET_PATTERN + "=/sample/sdn/nsc/*",
        HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" + HTTP_WHITEBOARD_CONTEXT_NAME + "=" + "OSC-API" + ")",
        HTTP_WHITEBOARD_TARGET + "=(" + "org.apache.felix.http.name" + "=" + "OSC-API" + ")" })

public class SampleSdnServletDelegate extends ResourceConfig implements Servlet {

    static final long serialVersionUID = 1L;

    @Reference
    private InspectionPortApis inspectionPortApis;

    @Reference
    private NetworkElementApis networkElementApis;

    @Reference
    private InspectionHookApis inspectionHookApis;

    /** The Jersey REST container */
    private ServletContainer container;

    @Activate
    void activate() throws Exception {

        super.register(JacksonJaxbJsonProvider.class);
        super.property(ServerProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        super.registerInstances(this.inspectionPortApis, this.networkElementApis, this.inspectionHookApis);
        this.container = new ServletContainer(this);
    }

    @Override
    public void destroy() {
        this.container.destroy();
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.container.getServletConfig();
    }

    @Override
    public String getServletInfo() {
        return this.container.getServletInfo();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.container.init(config);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        this.container.service(request, response);
    }
}
