package sonia.scm.crowd.user;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.service.client.CrowdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.crowd.CrowdAuthenticationHandler;
import sonia.scm.search.SearchRequest;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserManagerDecorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrowdUserManagerDecorator extends UserManagerDecorator {

	/** the logger for CrowdGroupManager */
	private static final Logger logger = LoggerFactory.getLogger(CrowdUserManagerDecorator.class);

	/** crowdAutentication Handler */
	private CrowdAuthenticationHandler crowdAuthenticationHandler;

	// ~--- constructors
	// ---------------------------------------------------------

	public CrowdUserManagerDecorator(UserManager userManager, CrowdAuthenticationHandler crowdAuthenticationHandler) {
		super(userManager);
		this.crowdAuthenticationHandler= crowdAuthenticationHandler;
	}

	// ~--- methods
	// --------------------------------------------------------------

	/**
	 * Search in {@link SearchRequest} and in Crowd Groups
	 */
	@Override
	public Collection<User> search(SearchRequest searchRequest) {

		Collection<User> users = super.search(searchRequest);

		users.addAll(searchInCrowd(searchRequest));

		return users;
	}

	/**
	 * Request Crowd to get the users
	 * 
	 * @param searchRequest
	 *            search criteria
	 * @return List of user which match with the criteria
	 */
	private Collection<? extends User> searchInCrowd(SearchRequest searchRequest) {

		Collection<User> users = new ArrayList<User>();

		CrowdClient crowdClient = crowdAuthenticationHandler.getCrowdClient();

		try {
			List<com.atlassian.crowd.model.user.User> searchUsers = crowdClient.searchUsers(new TermRestriction<String>(GroupTermKeys.NAME, MatchMode.STARTS_WITH, searchRequest.getQuery()), 0, -1);

			for (com.atlassian.crowd.model.user.User userCrowd : searchUsers) {
				users.add(new User(userCrowd.getName(), userCrowd.getDisplayName(), userCrowd.getEmailAddress()));

			}
		} catch (OperationFailedException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Operation failed: " + e.getMessage());
			}
		} catch (InvalidAuthenticationException iae) {
			if (logger.isDebugEnabled()) {
				logger.debug("Incorrect credentials");
			}
		} catch (ApplicationPermissionException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Application not permitted to perform authentication in crowd");
			}
		}

		return users;
	}
}
