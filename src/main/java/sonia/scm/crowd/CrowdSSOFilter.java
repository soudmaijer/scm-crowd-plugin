package sonia.scm.crowd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.filter.HttpFilter;
import sonia.scm.web.security.WebSecurityContext;

import javax.inject.Provider;
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

    /**
     * @param securityContextProvider
     */
    @Inject
    public CrowdSSOFilter(final Provider<WebSecurityContext> securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    /**
     * {@inheritDoc}
     */
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException {

        try {
            WebSecurityContext securityContext = securityContextProvider.get();
            if (!securityContext.isAuthenticated()) {
                if (log.isDebugEnabled()) {
                    log.debug("Current user is not authenticated, trying Crowd SSO.");
                }
                securityContext.authenticate(request, response, CrowdAuthenticationHandler.CROWD_SSO, CrowdAuthenticationHandler.CROWD_SSO);
            }
        } finally {
            chain.doFilter(request, response);
        }
    }

    /**
     * The logger for CrowdAuthenticationHandler
     */
    private static final Logger log = LoggerFactory.getLogger(CrowdSSOFilter.class);

    private final Provider<WebSecurityContext> securityContextProvider;
}