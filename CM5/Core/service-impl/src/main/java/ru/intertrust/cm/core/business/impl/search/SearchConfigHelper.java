package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.search.DomainObjectFilterConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.model.FatalException;

public class SearchConfigHelper {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    public static class SearchAreaDetailsConfig {

        private IndexedDomainObjectConfig objectConfig;
        private String areaName;
        private String targetObjectType;

        public IndexedDomainObjectConfig getObjectConfig() {
            return objectConfig;
        }

        public String getAreaName() {
            return areaName;
        }

        public String getTargetObjectType() {
            return targetObjectType;
        }
    }

    public List<SearchAreaDetailsConfig> findEffectiveConfigs(DomainObject object) {
        ArrayList<SearchAreaDetailsConfig> result = new ArrayList<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            processConfigList(object, area.getName(), null, area.getTargetObjects(), result);
        }
        return result;
    }

    private void processConfigList(DomainObject object, String areaName, String targetObjectType,
            Collection<? extends IndexedDomainObjectConfig> list, ArrayList<SearchAreaDetailsConfig> result) {
        for (IndexedDomainObjectConfig config : list) {
            if (targetObjectType == null) {
                targetObjectType = config.getType();
            }
            if (object.getTypeName().equalsIgnoreCase(config.getType())) {
                DomainObjectFilter filter = createFilter(config);
                if (filter == null || filter.filter(object)) {
                    SearchAreaDetailsConfig details = new SearchAreaDetailsConfig();
                    details.objectConfig = config;
                    details.areaName = areaName;
                    details.targetObjectType = targetObjectType;
                    result.add(details);
                }
            }
            processConfigList(object, areaName, targetObjectType, config.getLinkedObjects(), result);
        }
    }

    @SuppressWarnings("unchecked")
    private DomainObjectFilter createFilter(IndexedDomainObjectConfig config) {
        DomainObjectFilterConfig filterConfig = config.getFilter();
        if (filterConfig == null) {
            return null;
        }
        if (filterConfig.getJavaClass() != null) {
            try {
                Class<? extends DomainObjectFilter> clazz =
                        (Class<? extends DomainObjectFilter>) Class.forName(filterConfig.getJavaClass());
                return clazz.newInstance();
            } catch (Exception e) {
                throw new FatalException("Error creating search area filter: " + filterConfig.getJavaClass(), e);
            }
        }
        return null;    //*****
    }
}
