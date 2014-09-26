package ru.intertrust.cm.core.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

public class ConfigurationStorage {

    public ConfigurationStorage(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration configuration;

    public Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    public Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    public Map<FieldConfigKey, CollectionColumnConfig> collectionColumnConfigMap = new HashMap<>();

    public CaseInsensitiveMap<Collection<DomainObjectTypeConfig>> directChildDomainObjectTypesMap = new CaseInsensitiveMap<>();
    public CaseInsensitiveMap<Collection<DomainObjectTypeConfig>> indirectChildDomainObjectTypesMap = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<ToolBarConfig> toolbarConfigByPluginMap = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<List<DynamicGroupConfig>> dynamicGroupConfigByContextMap = new CaseInsensitiveMap<>();
    public Map<FieldConfigKey, List<DynamicGroupConfig>> dynamicGroupConfigsByTrackDOMap = new HashMap<>();

    public Map<FieldConfigKey, AccessMatrixStatusConfig> accessMatrixByObjectTypeAndStatusMap = new HashMap<>();

    public CaseInsensitiveMap<String> matrixReferenceTypeNameMap = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<Boolean> readPermittedToEverybodyMap = new CaseInsensitiveMap<>();

    public GlobalSettingsConfig globalSettings;

    public CaseInsensitiveMap<String> attachmentDomainObjectTypes = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<String[]> domainObjectTypesHierarchy = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<LogDomainObjectAccessConfig> eventLogDomainObjectAccessConfig = new CaseInsensitiveMap<>();
}
