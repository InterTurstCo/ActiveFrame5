package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.ConcurrentCaseInsensitiveMap;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class ConfigurationStorage {

    Configuration configuration;

    Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    Map<FieldConfigKey, CollectionColumnConfig> collectionColumnConfigMap = new HashMap<>();

    CaseInsensitiveMap<Collection<DomainObjectTypeConfig>> directChildDomainObjectTypesMap = new CaseInsensitiveMap<>();
    CaseInsensitiveMap<Collection<DomainObjectTypeConfig>> indirectChildDomainObjectTypesMap = new CaseInsensitiveMap<>();

    CaseInsensitiveMap<ToolBarConfig> toolbarConfigByPluginMap = new CaseInsensitiveMap<>();

    CaseInsensitiveMap<List<DynamicGroupConfig>> dynamicGroupConfigByContextMap = new CaseInsensitiveMap<>();
    ConcurrentMap<FieldConfigKey, List<DynamicGroupConfig>> dynamicGroupConfigsByTrackDOMap = new ConcurrentHashMap<>();

    ConcurrentMap<FieldConfigKey, AccessMatrixStatusConfig> accessMatrixByObjectTypeAndStatusMap = new ConcurrentHashMap<>();

    ConcurrentCaseInsensitiveMap<String> matrixReferenceTypeNameMap = new ConcurrentCaseInsensitiveMap<>();

    Map<String, Boolean> readPermittedToEverybodyMap = new HashMap<>();

    GlobalSettingsConfig globalSettings;
    SqlTrace sqlTrace;

    CaseInsensitiveMap<String> attachmentDomainObjectTypes = new CaseInsensitiveMap<>();

    ConcurrentCaseInsensitiveMap<String[]> domainObjectTypesHierarchy = new ConcurrentCaseInsensitiveMap<>();

}
