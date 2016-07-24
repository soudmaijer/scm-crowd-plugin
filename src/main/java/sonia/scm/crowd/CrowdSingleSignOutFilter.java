package sonia.scm.crowd;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.web.filter.HttpFilter;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.CrowdException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Performs SSO logout from Crowd.
 */
@Singleton
public class CrowdSingleSignOutFilter extends HttpFilter {
    private static final Logger log = LoggerFactory.getLogger(CrowdSingleSignOutFilter.class);
    private final CrowdAuthenticationHandler crowdAuthenticationHandler;

    /**
     * Default Constructor.
     */
    @Inject
    public CrowdSingleSignOutFilter(CrowdAuthenticationHandler crowdAuthenticationHandler) {
        this.crowdAuthenticationHandler = crowdAuthenticationHandler;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (crowdAuthenticationHandler.getCrowdHttpAuthenticator().checkAuthenticated(request, response).isAuthenticated()) {
                logout(request, response);
            }
        } catch (OperationFailedException e) {
            log.warn("Failed to check if authenticated. Crowd logout will not be performed.", e);
        }

        chain.doFilter(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            crowdAuthenticationHandler.getCrowdHttpAuthenticator().logout(request, response);
            log.debug("Successfully logged out from Crowd");
        } catch (ApplicationPermissionException e) {
            log.debug("Crowd application permission denied.", e);
        } catch (CrowdException e) {
            log.warn("Failed to logout from Crowd.", e);
        }
    }
}
