package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.model.FatalException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ConfigurationExplorerBuilder {

    private static final String ALL_STATUSES_SIGN = "*";
    private final static String GLOBAL_SETTINGS_CLASS_NAME = "ru.intertrust.cm.core.config.GlobalSettingsConfig";

    ConfigurationExplorerImpl configurationExplorer;
    private ConfigurationStorage configurationStorage;

    void buildConfigurationStorage(ConfigurationExplorerImpl configurationExplorer, Configuration configuration) {
        this.configurationExplorer = configurationExplorer;
        this.configurationStorage = new ConfigurationStorage();
        this.configurationStorage.configuration = configuration;
        this.configurationExplorer.setConfig(this.configurationStorage);

        initConfigurationMaps();
    }

    private void initConfigurationMaps() {
        if (configurationStorage.configuration == null) {
            throw new FatalException("Failed to initialize ConfigurationExplorerImpl because " +
                    "Configuration is null");
        }

        List<DomainObjectTypeConfig> attachmentOwnerDots = new ArrayList<>();
        for (TopLevelConfig config : configurationStorage.configuration.getConfigurationList()) {

            if (GLOBAL_SETTINGS_CLASS_NAME.equalsIgnoreCase(config.getClass().getCanonicalName())) {
                configurationStorage.globalSettings = (GlobalSettingsConfig) config;
            }
            fillTopLevelConfigMap(config);

            if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) config;
                fillFieldsConfigMap(domainObjectTypeConfig);
                if (domainObjectTypeConfig.getAttachmentTypesConfig() != null) {
                    attachmentOwnerDots.add(domainObjectTypeConfig);
                }
            } else if (CollectionViewConfig.class.equals(config.getClass())) {
                CollectionViewConfig collectionViewConfig = (CollectionViewConfig) config;
                fillCollectionColumnConfigMap(collectionViewConfig);
            } else if (AccessMatrixConfig.class.equals(config.getClass())) {
                AccessMatrixConfig accessMatrixConfig = (AccessMatrixConfig) config;
                fillReadPermittedToEverybodyMap(accessMatrixConfig);
            }
        }

        initConfigurationMapsOfAttachmentDomainObjectTypes(attachmentOwnerDots);
    }

    private void fillTopLevelConfigMap(TopLevelConfig config) {
        CaseInsensitiveMap<TopLevelConfig> typeMap = configurationStorage.topLevelConfigMap.get(config.getClass());
        if (typeMap == null) {
            typeMap = new CaseInsensitiveMap<>();
            configurationStorage.topLevelConfigMap.put(config.getClass(), typeMap);
        }
        typeMap.put(config.getName(), config);
    }

    private void fillSystemFields(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getSystemFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            if (GenericDomainObject.STATUS_DO.equals(domainObjectTypeConfig.getName())
                    && GenericDomainObject.STATUS_FIELD_NAME.equals(fieldConfig.getName())) {
                continue;
            }
            configurationStorage.fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
    }

    private void fillCollectionColumnConfigMap(CollectionViewConfig collectionViewConfig) {
        if (collectionViewConfig.getCollectionDisplayConfig() != null) {
            for (CollectionColumnConfig columnConfig : collectionViewConfig.getCollectionDisplayConfig().
                    getColumnConfig()) {
                FieldConfigKey fieldConfigKey =
                        new FieldConfigKey(collectionViewConfig.getName(), columnConfig.getField());
                configurationStorage.collectionColumnConfigMap.put(fieldConfigKey, columnConfig);

            }
        }
    }

    private void fillFieldsConfigMap(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            configurationStorage.fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
        fillSystemFields(domainObjectTypeConfig);
    }

    private void initConfigurationMapsOfAttachmentDomainObjectTypes(List<DomainObjectTypeConfig> ownerAttachmentDOTs) {
        if (ownerAttachmentDOTs == null || ownerAttachmentDOTs.isEmpty()) {
            return;
        }

        try {
            AttachmentPrototypeHelper factory = new AttachmentPrototypeHelper();
            for (DomainObjectTypeConfig domainObjectTypeConfig : ownerAttachmentDOTs) {
                for (AttachmentTypeConfig attachmentTypeConfig : domainObjectTypeConfig.getAttachmentTypesConfig()
                        .getAttachmentTypeConfigs()) {
                    DomainObjectTypeConfig attachmentDomainObjectTypeConfig =
                            factory.makeAttachmentConfig(attachmentTypeConfig.getName(),
                                    domainObjectTypeConfig.getName());
                    fillTopLevelConfigMap(attachmentDomainObjectTypeConfig);
                    fillFieldsConfigMap(attachmentDomainObjectTypeConfig);
                    configurationStorage.attachmentDomainObjectTypes.put(attachmentDomainObjectTypeConfig.getName(), attachmentDomainObjectTypeConfig.getName());
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    private void fillReadPermittedToEverybodyMap(AccessMatrixConfig accessMatrixConfig) {
        Boolean readEverybody = accessMatrixConfig.isReadEverybody();
        if (readEverybody != null) {
            configurationStorage.readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), readEverybody);
        } else {
            fillReadPermittedToEverybodyMapFromStatus(accessMatrixConfig);
        }
    }

    @Deprecated
    private void fillReadPermittedToEverybodyMapFromStatus(AccessMatrixConfig accessMatrixConfig) {
        for (AccessMatrixStatusConfig accessMatrixStatus : accessMatrixConfig.getStatus()) {
            if (ALL_STATUSES_SIGN.equals(accessMatrixStatus.getName()))
                for (BaseOperationPermitConfig permission : accessMatrixStatus.getPermissions()) {

                    if (ReadConfig.class.equals(permission.getClass())
                            && (Boolean.TRUE.equals(((ReadConfig) permission).isPermitEverybody()))) {
                        configurationStorage.readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), true);
                        return;
                    }
                }
            configurationStorage.readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), false);
        }
    }

    private class PrototypeHelper {
        private ByteArrayInputStream bis;

        private PrototypeHelper(String templateName) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            TopLevelConfig templateDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, templateName);
            oos.writeObject(templateDomainObjectTypeConfig);
            oos.close();
            bis = new ByteArrayInputStream(bos.toByteArray());
        }

        public DomainObjectTypeConfig makeDomainObjectTypeConfig(String name)
                throws IOException, ClassNotFoundException {
            bis.reset();
            DomainObjectTypeConfig cloneDomainObjectTypeConfig =
                    (DomainObjectTypeConfig) new ObjectInputStream(bis).readObject();
            cloneDomainObjectTypeConfig.setTemplate(false);
            cloneDomainObjectTypeConfig.setName(name);

            return cloneDomainObjectTypeConfig;
        }
    }

    private class AttachmentPrototypeHelper {
        private PrototypeHelper prototypeHelper;

        private AttachmentPrototypeHelper() throws IOException {
            prototypeHelper = new PrototypeHelper("Attachment");
        }

        public DomainObjectTypeConfig makeAttachmentConfig(String name, String ownerTypeName)
                throws IOException, ClassNotFoundException {
            DomainObjectTypeConfig cloneDomainObjectTypeConfig = prototypeHelper.makeDomainObjectTypeConfig(name);

            ReferenceFieldConfig ownerReferenceConfig = new ReferenceFieldConfig();
            ownerReferenceConfig.setName(ownerTypeName);
            ownerReferenceConfig.setType(ownerTypeName);
            cloneDomainObjectTypeConfig.getFieldConfigs().add(ownerReferenceConfig);

            return cloneDomainObjectTypeConfig;
        }
    }
}
