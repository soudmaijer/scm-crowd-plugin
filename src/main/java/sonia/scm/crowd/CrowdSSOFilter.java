package sonia.scm.crowd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.filter.HttpFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Performs authentication based on the Crowd SSO cookie value.
 */
@Singleton
public class CrowdSSOFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(CrowdSSOFilter.class);
    private final CrowdAuthenticationHandler crowdAuthenticationHandler;

    /**
     * Default Constructor
     */
    @Inject
    public CrowdSSOFilter(CrowdAuthenticationHandler crowdAuthenticationHandler) {
        this.crowdAuthenticationHandler = crowdAuthenticationHandler;
    }

    /**
     * {@inheritDoc}
     */
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            Subject currentUser = SecurityUtils.getSubject();

            if (!currentUser.isAuthenticated()) {
                if( crowdAuthenticationHandler.requestContainsToken(request) ) {
                    if (log.isDebugEnabled()) {
                        log.debug("Current user is not authenticated, trying Crowd SSO.");
                    }
                    AuthenticationToken at = new UsernamePasswordToken(CrowdAuthenticationHandler.CROWD_SSO, CrowdAuthenticationHandler.CROWD_SSO);
                    SecurityUtils.getSecurityManager().authenticate(at);
                }
            }
        } finally {
            chain.doFilter(request, response);
        }
    }
}