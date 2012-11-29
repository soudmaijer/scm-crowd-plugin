package sonia.scm.crowd.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.crowd.CrowdAuthenticationHandler;
import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupListener;
import sonia.scm.group.GroupManager;
import sonia.scm.search.SearchRequest;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.service.client.CrowdClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CrowdGroupManager extends DefaultGroupManager implements GroupManager {

	/** the logger for CrowdGroupManager */
	private static final Logger logger = LoggerFactory.getLogger(CrowdGroupManager.class);

    /** Inject the crowdAutentication Handler */
	@Inject
	CrowdAuthenticationHandler crowdAuthenticationHandler;

	
	//~--- constructors ---------------------------------------------------------

	@Inject
	public CrowdGroupManager(GroupDAO groupDAO, Provider<Set<GroupListener>> groupListenerProvider) {
		super(groupDAO, groupListenerProvider);
	}
	
	
    //~--- methods --------------------------------------------------------------

    /**
     * Search in {@link DefaultGroupManager} and in Crowd Groups
     */
	@Override
	public Collection<Group> search(final SearchRequest searchRequest) {

		Collection<Group> groups = new ArrayList<Group>();

		groups.addAll(super.search(searchRequest));

		groups.addAll(searchInCrowd(searchRequest));

		return groups;
	}

	/** 
	 * Request Crowd to get the groups
	 *  
	 * @param searchRequest search criteria
	 * @return List of group which match with the criteria
	 */
	private Collection<? extends Group> searchInCrowd(SearchRequest searchRequest) {

		Collection<Group> groups = new ArrayList<Group>();

		CrowdClient crowdClient = crowdAuthenticationHandler.getCrowdClient();

		try {
			List<com.atlassian.crowd.model.group.Group> searchGroups = crowdClient.searchGroups(new TermRestriction(GroupTermKeys.NAME, MatchMode.STARTS_WITH, searchRequest.getQuery()), 0, -1);

			for (com.atlassian.crowd.model.group.Group groupCrowd : searchGroups) {
				groups.add(new Group(CrowdAuthenticationHandler.TYPE, groupCrowd.getName()));

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

		return groups;
	}

}
