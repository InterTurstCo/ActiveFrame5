package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.doel.DoelValidator;
import ru.intertrust.cm.core.config.search.DomainObjectFilterConfig;
import ru.intertrust.cm.core.config.search.IndexedContentConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
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
            for (IndexedContentConfig contentConfig : config.getContentObjects()) {
                if (object.getTypeName().equalsIgnoreCase(contentConfig.getType())) {
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

    private SearchFieldType getFieldType(FieldType type, boolean multiValued) {
        if (FieldType.DATETIME == type ||
            FieldType.DATETIMEWITHTIMEZONE == type ||
            FieldType.TIMELESSDATE == type) {
            return multiValued ? SearchFieldType.DATE_MULTI : SearchFieldType.DATE;
        }
        if (FieldType.LONG == type ||
            FieldType.DECIMAL == type) {
            return multiValued ? SearchFieldType.LONG_MULTI : SearchFieldType.LONG;
        }
        return multiValued ? SearchFieldType.TEXT_MULTI : SearchFieldType.TEXT;
    }

    public SearchFieldType getFieldType(IndexedFieldConfig config, String objectType) {
        if (config.getDoel() != null) {
            DoelExpression expr = DoelExpression.parse(config.getDoel());
            DoelValidator.DoelTypes analyzed = DoelValidator.validateTypes(expr, objectType);
            if (!analyzed.isCorrect()) {
                //TODO Report error message
                return null;
            }
            Set<FieldType> types = analyzed.getResultTypes();
            if (types.size() > 1) {
                return SearchFieldType.TEXT_MULTI;
            }
            return getFieldType(types.iterator().next(), !analyzed.isSingleResult());
        } else {
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(objectType.toLowerCase(), config.getName().toLowerCase());
            if (fieldConfig == null) {
                throw new IllegalArgumentException(config.getName() + " doesn't defined in type " + objectType);
            }
            return getFieldType(fieldConfig.getFieldType(), false);
        }
    }

    public Set<SearchFieldType> getFieldTypes(String name, Set<String> areas) {
        Set<SearchFieldType> types = new HashSet<>();
        Collection<SearchAreaConfig> allAreas = configurationExplorer.getConfigs(SearchAreaConfig.class);
        for (SearchAreaConfig area : allAreas) {
            if (areas.contains(area.getName())) {
                findFieldTypes(name, area.getTargetObjects(), types);
            }
        }
        return types;
    }

    private void findFieldTypes(String fieldName, Collection<? extends IndexedDomainObjectConfig> configs,
            Set<SearchFieldType> types) {
        for (IndexedDomainObjectConfig config : configs) {
            for (IndexedFieldConfig field : config.getFields()) {
                if (fieldName.equalsIgnoreCase(field.getName())) {
                    types.add(getFieldType(field, config.getType()));
                    break;      // No more fields with this name should be in this object config
                }
            }
            findFieldTypes(fieldName, config.getLinkedObjects(), types);
        }
    }

    public boolean isAttachmentObject(DomainObject object) {
        String type = object.getTypeName();
        return configurationExplorer.isAttachmentType(type);
    }

    public String getAttachmentParentLinkName(String parentType) {
        return parentType.toLowerCase();
    }

    public DomainObjectTypeConfig getTargetObjectType(String collectionName) {
        CollectionConfig collConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        return configurationExplorer.getConfig(DomainObjectTypeConfig.class, collConfig.getName()); //*****
    }
}
