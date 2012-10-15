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

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Configuration class for the Crowd integration. For Crowd integration  the minimal required
 * properties are:</p>
 * <p/>
 * <ul>
 * <li>Crowd application name</li>
 * <li>Crowd application password</li>
 * <li>Crowd server url</li>
 * </ul>
 * <p>For more information see: http://confluence.atlassian.com/display/CROWD/The+crowd.properties+File</p>
 *
 * @author Stephan Oudmaijer
 */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrowdPluginConfig {

    //~--- get methods ----------------------------------------------------------

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationPassword() {
        return applicationPassword;
    }

    public String getCrowdServerUrl() {
        return crowdServerUrl;
    }

    public String getSessionValidationinterval() {
        return sessionValidationinterval;
    }

    public String getSessionLastvalidation() {
        return sessionLastvalidation;
    }

    public String getCookieTokenkey() {
        return cookieTokenkey;
    }

    public String getHttpMaxConnections() {
        return httpMaxConnections;
    }

    public String getHttpTimeout() {
        return httpTimeout;
    }

    //~--- set methods ----------------------------------------------------------

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setApplicationPassword(String applicationPassword) {
        this.applicationPassword = applicationPassword;
    }

    public void setCrowdServerUrl(String crowdServerUrl) {
        this.crowdServerUrl = crowdServerUrl;
    }

    public void setSessionValidationinterval(String sessionValidationinterval) {
        this.sessionValidationinterval = sessionValidationinterval;
    }

    public void setSessionLastvalidation(String sessionLastvalidation) {
        this.sessionLastvalidation = sessionLastvalidation;
    }

    public void setCookieTokenkey(String cookieTokenkey) {
        this.cookieTokenkey = cookieTokenkey;
    }

    public void setHttpMaxConnections(String httpMaxConnections) {
        this.httpMaxConnections = httpMaxConnections;
    }

    public void setHttpTimeout(String httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    //~--- fields ---------------------------------------------------------------

    /**
     * Crowd applicationName
     */
    @XmlElement(name = "applicationName")
    private String applicationName = "scm-manager";
    /**
     * Crowd applicationPassword
     */
    @XmlElement(name = "applicationPassword")
    private String applicationPassword = "scm-manager";
    /**
     * Crowd serverUrl where the REST services are located.
     */
    @XmlElement(name = "crowdServerUrl")
    private String crowdServerUrl = "http://localhost/crowd/";
    /**
     * The number of minutes to cache authentication validation in the session. If this value is set to 0, each HTTP request will be authenticated with the Crowd server.
     */
    @XmlElement(name = "sessionValidationinterval")
    private String sessionValidationinterval = "15";
    /**
     * The session key to use when storing a Date value of the user's last authentication. 
     */
    @XmlElement(name = "sessionLastvalidation")
    private String sessionLastvalidation = "session.lastvalidation";
    /**
     * When using Crowd for single sign-on (SSO), you can specify the SSO cookie name for each application. Under the standard configuration, Crowd will use a single, default cookie name for all Crowd-connected applications. You can override the default with your own cookie name.
     * As well as allowing you to define the SSO cookie name, this feature also allows you to divide your applications into different SSO groups. For example, you might use one SSO token for your public websites and another for your internal websites.
     */
    @XmlElement(name = "cookieTokenkey")
    private String cookieTokenkey = "crowd.token_key";
    /**
     * The maximum number of HTTP connections in the connection pool for communication with the Crowd server.
     */
    @XmlElement(name = "httpMaxConnections")
    private String httpMaxConnections = "20";
    /**
     * The HTTP connection timeout (milliseconds) used for communication with the Crowd server. A value of zero indicates that there is no connection timeout.
     */
    @XmlElement(name = "httpTimeout")
    private String httpTimeout = "5000";
}