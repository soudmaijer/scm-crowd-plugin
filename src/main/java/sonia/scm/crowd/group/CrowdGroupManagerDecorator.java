package sonia.scm.crowd.group;

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
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupManagerDecorator;
import sonia.scm.search.SearchRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrowdGroupManagerDecorator extends GroupManagerDecorator {

	/** the logger for CrowdGroupManager */
	private static final Logger logger = LoggerFactory.getLogger(CrowdGroupManagerDecorator.class);

	/** crowdAutentication Handler */
	private CrowdAuthenticationHandler crowdAuthenticationHandler;

    //~--- constructors ---------------------------------------------------------

	public CrowdGroupManagerDecorator(GroupManager groupManager,CrowdAuthenticationHandler crowdAuthenticationHandler) {
		super(groupManager);
		this.crowdAuthenticationHandler=crowdAuthenticationHandler;
	}

	
    //~--- methods --------------------------------------------------------------

	/**
	 * Search in {@link SearchRequest} and in Crowd Groups
	 */
	@Override
	public Collection<Group> search(SearchRequest searchRequest) {

		Collection<Group> groups = super.search(searchRequest);

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
			List<com.atlassian.crowd.model.group.Group> searchGroups = crowdClient.searchGroups(new TermRestriction<String>(GroupTermKeys.NAME, MatchMode.STARTS_WITH, searchRequest.getQuery()), 0, -1);

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
