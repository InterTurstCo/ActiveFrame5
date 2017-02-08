package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveHashMap;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;

import java.util.*;

public class ConfigurationStorage {

    public ConfigurationStorage(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration configuration;

    public Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelConfigMap = new HashMap<>();

    public Map<Pair<String, Class<?>>, CaseInsensitiveMap<LocalizableConfig>> localizedConfigMap = new HashMap<>();

    public Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    public CaseInsensitiveMap<Set<String>> fieldNamesByTypeMap = new CaseInsensitiveMap<>();
    public CaseInsensitiveMap<List<FieldConfig>> mutableFieldsNoInheritanceMap = new CaseInsensitiveMap<>();
    public Map<FieldConfigKey, CollectionColumnConfig> collectionColumnConfigMap = new HashMap<>();

    public Map<FieldConfigKey, String> typeInHierarchyHavingFieldMap = new HashMap<>();
    public CaseInsensitiveMap<Set<ReferenceFieldConfig>> referenceFieldsMap = new CaseInsensitiveMap<>();
    public CaseInsensitiveMap<Set<ReferenceFieldConfig>> immutableReferenceFieldsMap = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<Collection<DomainObjectTypeConfig>> directChildDomainObjectTypesMap = new CaseInsensitiveMap<>();
    public CaseInsensitiveMap<Collection<DomainObjectTypeConfig>> indirectChildDomainObjectTypesMap = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<ToolBarConfig> toolbarConfigByPluginMap = new CaseInsensitiveMap<>();
    public Map<String, CaseInsensitiveMap<ToolBarConfig>> localizedToolbarConfigMap = new HashMap<>(); //<Locale, Map<plugin name, toolbarConfig>>

    public CaseInsensitiveMap<List<DynamicGroupConfig>> dynamicGroupConfigByContextMap = new CaseInsensitiveMap<>();
    public Map<FieldConfigKey, List<DynamicGroupConfig>> dynamicGroupConfigsByTrackDOMap = new HashMap<>();

    public Map<FieldConfigKey, AccessMatrixStatusConfig> accessMatrixByObjectTypeAndStatusMap = new HashMap<>();

    public CaseInsensitiveMap<String> matrixReferenceTypeNameMap = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<Set<String>> typesDelegatingAccessCheckTo = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<Set<String>> typesDelegatingAccessCheckToInLowerCase = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<Boolean> readPermittedToEverybodyMap = new CaseInsensitiveMap<>();

    public GlobalSettingsConfig globalSettings;

    public CaseInsensitiveMap<String> attachmentDomainObjectTypes = new CaseInsensitiveMap<>();
    
    public CaseInsensitiveMap<String> auditLogTypes = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<String[]> domainObjectTypesHierarchy = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<String[]> domainObjectTypesHierarchyBeginningFromType = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<LogDomainObjectAccessConfig> eventLogDomainObjectAccessConfig = new CaseInsensitiveMap<>();

    public CaseInsensitiveMap<FormConfig> collectedFormConfigMap = new CaseInsensitiveMap<>();

    public Map<String, CaseInsensitiveMap<FormConfig>> localizedCollectedFormConfigMap = new HashMap<>(); //<Locale, Map<form name, formConfig>>

    public CaseInsensitiveHashMap<AccessMatrixConfig> accessMatrixByObjectTypeUsingExtensionMap = new CaseInsensitiveHashMap<>();

    public CaseInsensitiveHashMap<List<String>> allowedToCreateUserGroupsMap = new CaseInsensitiveHashMap<>();

}
