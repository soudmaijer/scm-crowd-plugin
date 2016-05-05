package sonia.scm.crowd;

import com.google.inject.servlet.ServletModule;
import sonia.scm.plugin.ext.Extension;

@Extension
public class CrowdSSOServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/api/rest/authentication/logout*").through(CrowdSingleSignOutFilter.class);
    }
}

