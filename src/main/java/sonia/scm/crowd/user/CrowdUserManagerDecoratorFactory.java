package sonia.scm.crowd.user;

import com.google.inject.Inject;
import sonia.scm.crowd.CrowdAuthenticationHandler;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserManagerDecoratorFactory;

@Extension
public class CrowdUserManagerDecoratorFactory implements UserManagerDecoratorFactory {

	/** Inject the crowdAutentication Handler */
	@Inject
	private CrowdAuthenticationHandler crowdAuthenticationHandler;
	
	/**
	 * Define the new Crowd Decorator for Users
	 */
	@Override
	public UserManager createDecorator(UserManager userManager) {
		return new CrowdUserManagerDecorator(userManager,crowdAuthenticationHandler);
	}

}
