package ru.intertrust.cm.core.gui.impl.server.counters;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ComponentName("collection_counters_handler")
public class CollectionCountersHandler implements ComponentHandler {

    @Autowired
    CollectionsService collectionsService;
    @Autowired
    CrudService crudService;
    @Autowired
    ConfigurationExplorer configurationService;
    @Autowired
    CurrentUserAccessor currentUserAccessor;
    @Autowired
    PersonService personService;

    public CollectionCountersResponse getCounters(Dto input) {

        CollectionCountersRequest request = (CollectionCountersRequest) input;
        Map<CounterKey, Id> counterKeys = request.getCounterKeys();
        Map<CounterKey, Id> countersDomainObjects = obtainCountersFromDb(counterKeys);
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class, "business_universe");
        Integer cacheUpdateTime = businessUniverseConfig.getCollectionCountCacheRefreshConfig().getTime();
        Long cacheUpdateTimeMillis = TimeUnit.SECONDS.toMillis(cacheUpdateTime);
        CollectionCountersResponse countersResponse = new CollectionCountersResponse();

        long lastUpdatedTime = request.getLastUpdatedTime();
        if (updateNeeded(lastUpdatedTime, cacheUpdateTimeMillis)) {
            recalculateCounters(countersDomainObjects);
            countersResponse.setLastUpdatedTime(System.currentTimeMillis());

        } else {
            countersResponse.setLastUpdatedTime(lastUpdatedTime);
        }
        Map<CounterKey, Long> counterNumericValues = transformToPresentationForm(countersDomainObjects);
        countersResponse.setCounterValues(counterNumericValues);
        countersResponse.setCounterServerObjectIds(countersDomainObjects);
        return countersResponse;
    }

    private Map<CounterKey, Id> obtainCountersFromDb(Map<CounterKey, Id> links) {
        Map<CounterKey, Id> counters = new HashMap<>();
        for (Map.Entry<CounterKey, Id> counterKeyIdEntry : links.entrySet()) {
            Id value = counterKeyIdEntry.getValue();
            CounterKey key = counterKeyIdEntry.getKey();
            if (value != null) {
                DomainObject counterDomainObject = crudService.find(value);
                counters.put(key, counterDomainObject.getId());
            } else {
                String currentUser = currentUserAccessor.getCurrentUser();

                DomainObject navLinkCollectionObject = getBUNavigationLinkCollectionObject(key,
                        currentUser, queryFilters(currentUser, key.getLinkName()));
                counters.put(key, navLinkCollectionObject.getId());
            }
        }
        ;
        return counters;
    }

    private DomainObject getBUNavigationLinkCollectionObject(CounterKey key, String currentUser, List<Filter> filters) {
        IdentifiableObjectCollection bu_nav_link_collections = collectionsService.
                findCollection("bu_nav_link_collections", null, filters);
        DomainObject navLinkCollectionObject;
        if (bu_nav_link_collections.size() > 0) {
            IdentifiableObject identifiableObject = bu_nav_link_collections.get(0);
            navLinkCollectionObject = crudService.find(identifiableObject.getId());
        } else {
            navLinkCollectionObject = crudService.createDomainObject("bu_nav_link_collection");
            navLinkCollectionObject.setString("link", key.getLinkName());
            DomainObject person = personService.findPersonByLogin(currentUser);
            navLinkCollectionObject.setReference("person", person);
            navLinkCollectionObject.setLong("collection_count", 0L);
            navLinkCollectionObject = crudService.save(navLinkCollectionObject);
        }
        return navLinkCollectionObject;
    }

    private List<Filter> queryFilters(String currentUser, String linkName) {
        Filter byLink = new Filter();
        byLink.setFilter("byLink");
        byLink.addStringCriterion(0, linkName);
        Filter byPerson = new Filter();
        byPerson.setFilter("byPerson");
        byPerson.addStringCriterion(0, currentUser);
        return Arrays.asList(byLink, byPerson);
    }

    private Map<CounterKey, Long> transformToPresentationForm(Map<CounterKey, Id> countersDomainObjects) {
        Map<CounterKey, Long> counters = new HashMap<>();
        for (Map.Entry<CounterKey, Id> counterKeyDomainObjectEntry : countersDomainObjects.entrySet()) {
            CounterKey linkName = counterKeyDomainObjectEntry.getKey();
            DomainObject value = crudService.find(counterKeyDomainObjectEntry.getValue());
            Long collectionCounter = value.getLong("collection_count");
            counters.put(linkName, collectionCounter);
        }
        return counters;
    }

    private void recalculateCounters(Map<CounterKey, Id> counters) {
        Map<String, Integer> localCollectionCountCache = new HashMap<>();
        for (Map.Entry<CounterKey, Id> linkConfigDomainObjectEntry : counters.entrySet()) {
            CounterKey counterKey = linkConfigDomainObjectEntry.getKey();
            String collectionName = counterKey.getCollectionName();
            int collectionCount = 0;
            if (localCollectionCountCache.containsKey(collectionName)) {
                collectionCount = localCollectionCountCache.get(collectionName);
            } else {
                collectionCount = collectionsService.findCollectionCount(collectionName, Collections.EMPTY_LIST);
                localCollectionCountCache.put(collectionName, collectionCount);
            }
            DomainObject navLinkCollectionObject = crudService.find(linkConfigDomainObjectEntry.getValue());
            if(!Long.valueOf(collectionCount).equals(navLinkCollectionObject.getLong("collection_count"))){
                navLinkCollectionObject.setLong("collection_count", Long.valueOf(collectionCount));
                crudService.save(navLinkCollectionObject);
            }
        }
    }

    private boolean updateNeeded(long lastUpdatedTime, Long cacheUpdateTime) {
        return (lastUpdatedTime + cacheUpdateTime) < System.currentTimeMillis();
    }

}
