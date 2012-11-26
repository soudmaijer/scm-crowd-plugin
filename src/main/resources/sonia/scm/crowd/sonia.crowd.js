/* *
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

Ext.ns("Sonia.crowd");

Sonia.crowd.ConfigPanel = Ext.extend(Sonia.config.ConfigForm, {

  titleText: 'Crowd Authentication',
  applicationNameAttributeText: 'Application Name',
  applicationPasswordAttributeText: 'Application Password',
  crowdServerUrlAttributeText: 'Crowd server URL',
  sessionValidationintervalAttributeText: 'Session validation interval',
  sessionValidationintervalAttributeHelpText: 'The number of minutes to cache authentication validation in the session. If this value is set to 0, each HTTP request will be authenticated with the Crowd server.',
  sessionLastvalidationText: 'Session key last validation',
  sessionLastvalidationHelpText: 'The session key to use when storing a Date value of the users last authentication.',
  cookieTokenkeyText: 'Cookie Tokenkey',
  cookieTokenkeyHelpText: 'When using Crowd for single sign-on (SSO), you can specify the SSO cookie name for each application. Under the standard configuration, Crowd will use a single, default cookie name for all Crowd-connected applications. You can override the default with your own cookie name. As well as allowing you to define the SSO cookie name, this feature also allows you to divide your applications into different SSO groups. For example, you might use one SSO token for your public websites and another for your internal websites.',
  httpProxyHostText: 'Proxy host',
  httpProxyPortText: 'Proxy port',
  httpProxyUsernameText: 'Proxy username',
  httpProxyPasswordText: 'Proxy password',
  httpMaxConnectionsText: 'Max connections',
  httpMaxConnectionsHelpText: 'The maximum number of HTTP connections in the connection pool for communication with the Crowd server.',
  httpTimeoutText: 'Connection timeout',
  httpTimeoutHelpText: 'The HTTP connection timeout (milliseconds) used for communication with the Crowd server. A value of zero indicates that there is no connection timeout.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      items : [{
        xtype : 'textfield',
        fieldLabel : this.applicationNameAttributeText,
        name : 'applicationName',
        allowBlank : true,
        helpText: this.applicationNameAttributeText
      },{
        xtype : 'textfield',
        inputType: 'password',
        fieldLabel : this.applicationPasswordAttributeText,
        name : 'applicationPassword',
        allowBlank : true,
        helpText: this.applicationPasswordAttributeText
      },{
        xtype : 'textfield',
        fieldLabel : this.crowdServerUrlAttributeText,
        name : 'crowdServerUrl',
        allowBlank : false,
        helpText: this.crowdServerUrlAttributeText
      },{
        xtype : 'textfield',
        fieldLabel : this.sessionValidationintervalAttributeText,
        name : 'sessionValidationinterval',
        allowBlank : false,
        helpText: this.sessionValidationintervalAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.sessionLastvalidationText,
        name : 'sessionLastvalidation',
        allowBlank : false,
        helpText: this.sessionLastvalidationHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.cookieTokenkeyText,
        name : 'cookieTokenkey',
        allowBlank : false,
        helpText: this.cookieTokenkeyHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.httpMaxConnectionsText,
        name : 'httpMaxConnections',
        allowBlank : false,
        helpText: this.httpMaxConnectionsHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.httpTimeoutText,
        name : 'httpTimeout',
        allowBlank : false,
        helpText: this.httpTimeoutHelpText
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.crowd.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },

  onSubmit: function(values){
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/crowd.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){
      el.mask(this.loadingText);
    }, 100);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/crowd.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        alert('failure');
      }
    });
  }

});

// register xtype
Ext.reg("crowdConfigPanel", Sonia.crowd.ConfigPanel);

// regist config panel
registerGeneralConfigPanel({
  id: 'crowdConfigPanel',
  xtype: 'crowdConfigPanel'
});