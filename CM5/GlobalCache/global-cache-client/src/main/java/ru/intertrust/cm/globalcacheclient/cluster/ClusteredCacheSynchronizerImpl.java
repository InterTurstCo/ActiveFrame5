package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Denis Mitavskiy Date: 05.08.2015 Time: 19:12
 */
@Stateless(name = "ClusteredCacheSynchronizer")
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(ClusteredCacheSynchronizer.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ClusteredCacheSynchronizerImpl implements ClusteredCacheSynchronizer {
	final static Logger logger = LoggerFactory.getLogger(ClusteredCacheSynchronizerImpl.class);

	@org.springframework.beans.factory.annotation.Value("${global.cache.topic.name:"
			+ GlobalCacheJmsHelper.NOTIFICATION_TOPIC + "}")
	private String clusterNotificationTopic;

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void notifyCommit(DomainObjectsModification modification, PersonAccessChanges personAccessChanges) {
		final boolean clearFullAccessLog = personAccessChanges.clearFullAccessLog();
		final Set<Id> personsWhosAccessRightsRulesChanged = personAccessChanges
				.getPersonsWhosAccessRightsRulesChanged();
		final HashSet<Id> personsWhosGroupsChanged = clearFullAccessLog ? null
				: (personsWhosAccessRightsRulesChanged instanceof HashSet
						? ((HashSet) personsWhosAccessRightsRulesChanged)
						: new HashSet<>(personsWhosAccessRightsRulesChanged));
		final HashMap<Id, HashMap<Id, Boolean>> personAccessByObject = personAccessChanges.getPersonAccessByObject();
		final Set<Id> changedAccessIds = personAccessByObject == null ? null : personAccessByObject.keySet();
		final List<Id> createdIds = modification.getCreatedIds();
		final Set<Id> savedAndChangedStatusIds = modification.getSavedAndChangedStatusIds();
		final Set<Id> deletedIds = modification.getDeletedIds();
		final List<Id> modifiedAutoDomainObjectIds = modification.getModifiedAutoDomainObjectIds();
		final HashSet<Id> createdIdsSet = new HashSet<>(createdIds);
		final HashSet<Id> ids = new HashSet<>((int) (1.5 * (createdIds.size() + changedAccessIds.size()
				+ savedAndChangedStatusIds.size() + deletedIds.size() + modifiedAutoDomainObjectIds.size())));
		ids.addAll(changedAccessIds);
		ids.addAll(savedAndChangedStatusIds);
		ids.addAll(deletedIds);
		ids.addAll(modifiedAutoDomainObjectIds);

		GlobalCacheJmsHelper.sendClusterNotification(
				new CacheInvalidation(createdIdsSet, ids, clearFullAccessLog, personsWhosGroupsChanged),
				clusterNotificationTopic);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void notifyClear() {
		GlobalCacheJmsHelper.sendClusterNotification(new CacheInvalidation(true), clusterNotificationTopic);
	}
}
