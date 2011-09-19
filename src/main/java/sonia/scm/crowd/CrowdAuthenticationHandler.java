package sonia.scm.crowd;

/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
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

import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticatorImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelperImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractorImpl;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.CrowdClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigChangedListener;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <p>Performs Crowd authentication. Populates the scm-manager user
 * Object with the Crowd user properties and Crowd groups assigned
 * to the user.</p>
 * <p/>
 * <p>Tested against Crowd 2.2.1</p>
 *
 * @author Stephan Oudmaijer
 */
@Singleton
@Extension
public class CrowdAuthenticationHandler implements AuthenticationHandler, ConfigChangedListener {

    //~--- constructors ---------------------------------------------------------

    /**
     * Constructs the CrowdAuthenticationHandler and loads the config from the store.
     *
     * @param scmConfiguration the global configuration.
     * @param storeFactory     store for the configuration.
     */
    @Inject
    public CrowdAuthenticationHandler(ScmConfiguration scmConfiguration, StoreFactory storeFactory) {
        this.scmConfiguration = scmConfiguration;
        this.scmConfiguration.addListener(this);
        store = storeFactory.getStore(CrowdPluginConfig.class, TYPE);
    }

    //~--- methods --------------------------------------------------------------

    /**
     * Authenticates the user in Crowd.
     *
     * @param request  http request
     * @param response http response
     * @param username username
     * @param password password
     * @return the result
     */
    @Override
    public AuthenticationResult authenticate(HttpServletRequest request,
                                             HttpServletResponse response, String username, String password) {

        try {

            com.atlassian.crowd.model.user.User crowdUser;

            // try SSO
            if (CROWD_SSO.equals(username) && CROWD_SSO.equals(password)) {
                username = null;
                password = null;
                if (logger.isDebugEnabled()) {
                    logger.debug("Crowd SSO authenticate for token: "+ crowdHttpAuthenticator.getToken(request));
                }
                crowdUser = crowdHttpAuthenticator.getUser(request);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Crowd authenticate for user: " + username);
                }

                AssertUtil.assertIsNotEmpty(username);
                AssertUtil.assertIsNotEmpty(password);
                crowdUser = crowdHttpAuthenticator.authenticate(request, response, username, password);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Crowd user: " + crowdUser);
            }
            if (crowdUser != null && crowdUser.isActive()) {
                List<com.atlassian.crowd.model.group.Group> groups = crowdClient.getGroupsForUser(crowdUser.getName(), 0, -1);
                return new AuthenticationResult(populateUser(crowdUser), populateGroups(groups));
            } else {
                return AuthenticationResult.NOT_FOUND;
            }

        } catch (UserNotFoundException unoe) {
            if (logger.isDebugEnabled()) {
                logger.debug("User not found in crowd: " + username);
            }
            return AuthenticationResult.NOT_FOUND;
        } catch (InactiveAccountException unoe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not an active user in crowd: " + username);
            }
            return AuthenticationResult.NOT_FOUND;
        } catch (InvalidAuthenticationException iae) {
            if (logger.isDebugEnabled()) {
                logger.debug("Incorrect credentials for user in crowd: " + username);
            }
            return AuthenticationResult.FAILED;
        } catch (ExpiredCredentialException iae) {
            if (logger.isDebugEnabled()) {
                logger.debug("Credentials expired for user in crowd: " + username);
            }
            return AuthenticationResult.FAILED;
        } catch (ApplicationPermissionException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Application not permitted to perform authentication in crowd");
            }
            return AuthenticationResult.FAILED;
        } catch (OperationFailedException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Operation failed: "+ e.getMessage());
            }
            return AuthenticationResult.FAILED;
        } catch (InvalidTokenException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid token found in request. "+ e.getMessage());
            }
            return AuthenticationResult.FAILED;
        } catch (ApplicationAccessDeniedException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Application not permitted to access Crowd: "+ e.getMessage());
            }
            return AuthenticationResult.FAILED;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to close
    }

    /**
     * Reinitialize the Crowd client on config changes.
     *
     * @param configuration the new configuration object.
     */
    @Override
    public void configChanged(Object configuration) {
        this.scmConfiguration = (ScmConfiguration) configuration;
        initCrowdClient();
    }

    /**
     * Initializes the plugin with the Crowd config.
     * Constructs a crowdClient that is reused.
     *
     * @param context The plugin context.
     */
    @Override
    public synchronized void init(SCMContextProvider context) {

        config = store.get();

        if (config == null) {
            config = new CrowdPluginConfig();
        }

        initCrowdClient();
    }

    /**
     * Initializes the Crowd client. Needs to be called
     * upon configuration changes.
     */
    private void initCrowdClient() {

        Properties p = new Properties();
        p.setProperty("crowd.server.url", config.getCrowdServerUrl());
        p.setProperty("application.name", config.getApplicationName());
        p.setProperty("application.password", config.getApplicationPassword());
        p.setProperty("session.validationinterval", config.getSessionValidationinterval());
        p.setProperty("cookie.tokenkey", config.getCookieTokenkey());
        p.setProperty("http.max.connections", config.getHttpMaxConnections());
        p.setProperty("http.timeout", config.getHttpTimeout());

        if (scmConfiguration.isEnableProxy()) {
            p.setProperty("http.proxy.host", scmConfiguration.getProxyServer());
            p.setProperty("http.proxy.port", String.valueOf(scmConfiguration.getProxyPort()));
            if (Util.isNotEmpty(scmConfiguration.getProxyUser())) {
                p.setProperty("http.proxy.username", scmConfiguration.getProxyUser());
            }
            if (Util.isNotEmpty(scmConfiguration.getProxyPassword())) {
                p.setProperty("http.proxy.password", scmConfiguration.getProxyPassword());
            }
        }

        ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromProperties(p);
        crowdClient = new RestCrowdClientFactory().newInstance(clientProperties);
        crowdHttpAuthenticator  = new CrowdHttpAuthenticatorImpl(crowdClient, clientProperties, CrowdHttpTokenHelperImpl.getInstance(CrowdHttpValidationFactorExtractorImpl.getInstance()));
    }

    /**
     * Saves the Crowd config.
     */
    public void storeConfig(CrowdPluginConfig config) {
        this.config = config;
        store.set(config);
        initCrowdClient();
    }

    //~--- get methods ----------------------------------------------------------

    /**
     * Returns the Crowd config.
     *
     * @return the Crowd configuration.
     */
    public CrowdPluginConfig getConfig() {
        return config;
    }

    /**
     * Method description
     *
     * @return the user type.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    //~--- set methods ----------------------------------------------------------

    /**
     * Set the Crowd config.
     *
     * @param config the Configuration to set.
     */
    public void setConfig(CrowdPluginConfig config) {
        this.config = config;
    }

    //~--- methods --------------------------------------------------------------

    /**
     * Populates the List with groups from Crowd Group objects. Scm manager
     * only needs String values.
     *
     * @param crowdGroups Crowd groups
     * @return List of String values
     */
    private List<String> populateGroups(List<com.atlassian.crowd.model.group.Group> crowdGroups) {
        List<String> groups = new ArrayList<String>();

        for (com.atlassian.crowd.model.group.Group crowdGroup : crowdGroups) {
            groups.add(crowdGroup.getName());
        }

        return groups;
    }

    /**
     * Populates an scm User with the properties of a Crowd User.
     *
     * @param crowdUser The Crowd user to copy the properties from. Should not be null.
     * @return a populated Crowd user.
     */
    private User populateUser(com.atlassian.crowd.model.user.User crowdUser) {
        User scmUser = new User();
        scmUser.setName(crowdUser.getName());
        scmUser.setDisplayName(crowdUser.getDisplayName());
        scmUser.setMail(crowdUser.getEmailAddress());
        return scmUser;
    }

    //~--- fields ---------------------------------------------------------------

    public static final String CROWD_SSO = "CROWD_SSO";

    /**
     * The logger for CrowdAuthenticationHandler
     */
    private static final Logger logger =
            LoggerFactory.getLogger(CrowdAuthenticationHandler.class);

    private CrowdClient crowdClient;
    private CrowdHttpAuthenticator crowdHttpAuthenticator;

    /**
     * The type of user.
     */
    public static final String TYPE = "crowd";

    /**
     * Crowd configuration.
     */
    private CrowdPluginConfig config;

    /**
     * Store for the Crowd configuration.
     */
    private Store<CrowdPluginConfig> store;

    /**
     * ScmManager configuration.
     */
    private ScmConfiguration scmConfiguration;
}
