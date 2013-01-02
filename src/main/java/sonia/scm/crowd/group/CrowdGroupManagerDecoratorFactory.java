package sonia.scm.crowd.group;

import com.google.inject.Inject;

import sonia.scm.crowd.CrowdAuthenticationHandler;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupManagerDecoratorFactory;
import sonia.scm.plugin.ext.Extension;

@Extension
public class CrowdGroupManagerDecoratorFactory implements GroupManagerDecoratorFactory {

	
	/** Inject the crowdAutentication Handler */
	@Inject
	private CrowdAuthenticationHandler crowdAuthenticationHandler;

	/**
	 * Define the new Crowd Decorator for Groups
	 */
	@Override
	public GroupManager createDecorator(GroupManager groupManager) {
		return new CrowdGroupManagerDecorator(groupManager,crowdAuthenticationHandler);
	}

}
