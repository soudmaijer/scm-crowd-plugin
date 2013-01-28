package sonia.scm.crowd;

/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import sonia.scm.security.Role;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 * <p>Rest service for the configuration options. See the General configuration
 * in the scm-manager admin section.</p>
 *
 * @author Stephan Oudmaijer
 */
@Singleton
@Path("config/auth/crowd")
public class CrowdPluginConfigResource {

    /**
     * Constructs the Config REST service.
     *
     * @param authenticationHandler CrowdAuthenticationHandler
     */
    @Inject
    public CrowdPluginConfigResource(CrowdAuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    //~--- get methods ----------------------------------------------------------

    /**
     * Returns the Crowd config.
     *
     * @return a config Object in JSON format.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CrowdPluginConfig getConfig() {
        SecurityUtils.getSubject().checkRole(Role.ADMIN);
        return authenticationHandler.getConfig();
    }

    //~--- set methods ----------------------------------------------------------

    /**
     * Stores the config
     *
     * @param uriInfo uriInfo
     * @param config the new configuration
     * @return the result
     * @throws IOException when response could not be created.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setConfig(@Context UriInfo uriInfo, CrowdPluginConfig config)
            throws IOException {
        SecurityUtils.getSubject().checkRole(Role.ADMIN);
        authenticationHandler.storeConfig(config);
        return Response.created(uriInfo.getRequestUri()).build();
    }

    //~--- fields ---------------------------------------------------------------

    /**
     * The CrowdAuthenticationHandler which is the actual plugin.
     */
    private CrowdAuthenticationHandler authenticationHandler;
}
