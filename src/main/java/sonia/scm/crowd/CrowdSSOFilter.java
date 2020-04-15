package sonia.scm.crowd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.user.User;
import sonia.scm.web.filter.AutoLoginModule;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Performs authentication based on the Crowd SSO cookie value.
 */
@Singleton
@Extension
public class CrowdSSOFilter implements AutoLoginModule {

    private static final Logger log = LoggerFactory.getLogger(CrowdSSOFilter.class);
    private final CrowdAuthenticationHandler crowdAuthenticationHandler;

    /**
     * Default Constructor
     */
    @Inject
    public CrowdSSOFilter(CrowdAuthenticationHandler crowdAuthenticationHandler) {
        this.crowdAuthenticationHandler = crowdAuthenticationHandler;
    }

    @Override
    public User authenticate(HttpServletRequest request, HttpServletResponse response, Subject subject) {
        try {
            Subject currentUser = SecurityUtils.getSubject();

            if (!currentUser.isAuthenticated()) {
                if (crowdAuthenticationHandler.requestContainsToken(request)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Current user is not authenticated, trying Crowd SSO.");
                    }
                    AuthenticationToken token = new UsernamePasswordToken(CrowdAuthenticationHandler.CROWD_SSO,
                            CrowdAuthenticationHandler.CROWD_SSO);

                    subject.login(token);
                    return subject.getPrincipals().oneByType(User.class);
                }
            }
        } catch (AuthenticationException authenticationException) {
            return null;
        }

        return null;
    }
}