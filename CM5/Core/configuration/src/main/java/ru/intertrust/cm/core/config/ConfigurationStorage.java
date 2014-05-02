package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ConfigurationStorage {

    Configuration configuration;

    Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    Map<FieldConfigKey, CollectionColumnConfig> collectionColumnConfigMap = new HashMap<>();

    Map<String, Boolean> readPermittedToEverybodyMap = new HashMap<>();

    GlobalSettingsConfig globalSettings;
    CaseInsensitiveMap<String> attachmentDomainObjectTypes = new CaseInsensitiveMap<>();

    CaseInsensitiveMap<String[]> domainObjectTypesHierarchy = new CaseInsensitiveMap<>();

}
