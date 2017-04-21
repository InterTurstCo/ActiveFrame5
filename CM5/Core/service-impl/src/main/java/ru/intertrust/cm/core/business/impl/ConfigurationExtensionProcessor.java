package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 20.04.2017
 *         Time: 13:33
 */
public class ConfigurationExtensionProcessor {
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationExtensionProcessor.class);
    public static final String CONFIGURATION_START = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<configuration xmlns=\"https://cm5.intertrust.ru/config\">";
    public static final String CONFIGURATION_END = "</configuration>";
    
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private ConfigurationSerializer configurationSerializer;
    @Autowired
    private DomainObjectDao domainObjectDao;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ApplicationContext context;

    @Transactional
    public void applyConfigurationExtension() {
        final ArrayList<TagInfo> activeExtensions = deleteOrDeactivateInvalidExtensions();
        createNewEntries();
        activateExtensionsOrDeactivateEverythingOnError(activeExtensions);
    }

    @Transactional
    public void activateDrafts(Collection<Id> toolingIds) {

    }

    private ArrayList<TagInfo> deleteOrDeactivateInvalidExtensions() {
        final List<DomainObject> extensionDOs = getAllConfigExtensionDomainObjects();
        final Map<String, TagTypeInfo> tagTypeByTagName = getTagClassMapping();
        ArrayList<DomainObject> toDeactivate = new ArrayList<>();
        ArrayList<DomainObject> toDeactivateAndClearXML = new ArrayList<>();
        ArrayList<Id> toDelete = new ArrayList<>();
        ArrayList<TagInfo> activeExtensions = new ArrayList<>();

        // get replaced and remove if they are not replaceable anymore
        // get "new" (not replaced) and remove if it's not allowed to create new anymore
        for (DomainObject extensionDO : extensionDOs) {
            final String tagType = extensionDO.getString("type");
            final String tagName = extensionDO.getString("name");
            final TagTypeInfo tagTypeInfo = tagTypeByTagName.get(tagType);
            if (tagTypeInfo == null) {
                logger.warn(getTagDefString(tagType, tagName) + " deleted - tag unknown");
                toDelete.add(extensionDO.getId());
                continue;
            }
            final TopLevelConfig distrConfig = (TopLevelConfig) configurationExplorer.getConfig(tagTypeInfo.clazz, tagName);
            if (distrConfig != null) {
                if (distrConfig.getReplacementPolicy() != TopLevelConfig.ExtensionPolicy.Runtime) {
                    logger.warn(getTagDefString(tagType, tagName) + " deleted - tag is not replaceable");
                    toDelete.add(extensionDO.getId());
                    continue;
                }
            } else if (tagTypeInfo.creationPolicy != TopLevelConfig.ExtensionPolicy.Runtime) {
                logger.warn(getTagDefString(tagType, tagName) + " deleted - new tags of this type are not allowed");
                toDelete.add(extensionDO.getId());
            }

            final boolean active = extensionDO.getBoolean("active");
            if (!active) {
                continue;
            }
            final String overriddenXml = extensionDO.getString("current_xml");
            if (overriddenXml == null || overriddenXml.trim().isEmpty()) {
                toDeactivate.add(extensionDO);
                continue;
            }
            final StringBuilder configString = new StringBuilder(CONFIGURATION_START.length() + overriddenXml.length() + CONFIGURATION_END.length());
            configString.append(CONFIGURATION_START);
            configString.append(overriddenXml);
            configString.append(CONFIGURATION_END);
            final Configuration configuration;
            TopLevelConfig extensionConfig = null;
            try {
                configuration = ConfigurationSerializer.deserializeConfiguration(configString.toString());
                if (configuration.getConfigurationList().size() != 1) {
                    logger.warn(getTagDefString(tagType, tagName) + " deactivated and XML cleared - XML doesn't define exactly 1 valid tag");
                    toDeactivateAndClearXML.add(extensionDO);
                    continue;
                }
            } catch (ConfigurationException e) {
                toDeactivate.add(extensionDO);
                logger.warn(getTagDefString(tagType, tagName) + " deactivated - XML is not valid", e);
                continue;
            }

            extensionConfig = configuration.getConfigurationList().get(0);
            if (extensionConfig.getClass() != tagTypeInfo.clazz) {
                toDeactivateAndClearXML.add(extensionDO);
                logger.warn(getTagDefString(tagType, tagName) + " deactivated and XML cleared - XML doesn't match the tag");
                continue;
            }
            if (!extensionConfig.getName().equals(tagName)) {
                toDeactivate.add(extensionDO);
                logger.warn(getTagDefString(tagType, tagName) + " deactivated - XML doesn't match name");
                continue;
            }

            activeExtensions.add(new TagInfo(extensionConfig, extensionDO));
        }

        for (DomainObject domainObject : toDeactivate) {
            domainObject.setBoolean("active", false);
        }
        for (DomainObject domainObject : toDeactivateAndClearXML) {
            domainObject.setBoolean("active", false);
            domainObject.setString("current_xml", null);
        }
        final AccessToken systemAccessToken = getSystemAccessToken();
        domainObjectDao.save(toDeactivate, systemAccessToken);
        domainObjectDao.save(toDeactivateAndClearXML, systemAccessToken);
        domainObjectDao.delete(toDelete, systemAccessToken);

        return activeExtensions;
    }

    private void createNewEntries() {
        final List<DomainObject> extensionDOs = getAllConfigExtensionDomainObjects();
        final Set<TagInfo> currentExtensions = new HashSet<>();
        for (DomainObject extensionDO : extensionDOs) {
            final String tagType = extensionDO.getString("type");
            final String tagName = extensionDO.getString("name");
            currentExtensions.add(new TagInfo(tagType, tagName));
        }
        final List<TopLevelConfig> distibutiveConfigs = configurationExplorer.getConfiguration().getConfigurationList();
        final ArrayList<DomainObject> newExtensibleTags = new ArrayList<>();
        for (TopLevelConfig distibutiveConfig : distibutiveConfigs) {
            final TagInfo newExtensibleTag = new TagInfo(distibutiveConfig, null);
            if (distibutiveConfig.getReplacementPolicy() == TopLevelConfig.ExtensionPolicy.Runtime && !currentExtensions.contains(newExtensibleTag)) {
                final GenericDomainObject domainObject = new GenericDomainObject("configuration_extension");
                domainObject.setString("type", newExtensibleTag.tagType);
                domainObject.setString("name", newExtensibleTag.name);
                domainObject.setBoolean("active", false);
                newExtensibleTags.add(domainObject);
            }
        }
        domainObjectDao.save(newExtensibleTags, getSystemAccessToken());
    }

    private void activateExtensionsOrDeactivateEverythingOnError(Collection<TagInfo> activeExtensions) {
        HashMap<TagInfo, TagInfo> extensionsMap = new HashMap<>(activeExtensions.size() * 2);
        for (TagInfo activeExtension : activeExtensions) {
            extensionsMap.put(activeExtension, activeExtension);
        }
        final ArrayList<TopLevelConfig> configurationList = new ArrayList<>(configurationExplorer.getConfiguration().getConfigurationList().size() + extensionsMap.size());
        for (TopLevelConfig topLevelConfig : configurationExplorer.getConfiguration().getConfigurationList()) {
            final TagInfo distrTagInfo = new TagInfo(topLevelConfig, null);
            final TagInfo extTagInfo = extensionsMap.get(distrTagInfo);
            if (extTagInfo != null) {
                configurationList.add(extTagInfo.topLevelConfig);
                extensionsMap.remove(extTagInfo);
            } else {
                configurationList.add(distrTagInfo.topLevelConfig);
            }
        }
        for (TagInfo tagInfo : extensionsMap.values()) {
            configurationList.add(tagInfo.topLevelConfig);
        }
        Configuration configuration = new Configuration();
        configuration.setConfigurationList(configurationList);
        try {
            ConfigurationExplorer newExplorer = new ConfigurationExplorerImpl(configuration, context, false);
            ((ConfigurationExplorerImpl) configurationExplorer).copyFrom(((ConfigurationExplorerImpl) newExplorer));
        } catch (Throwable e) {
            if (e instanceof FatalBeanException) {
                final Throwable cause = ((FatalBeanException) e).getCause();
                if (cause instanceof ConfigurationException) {
                    logger.error("Configuration extension not applied. List of errors:");
                    logger.error(((ConfigurationException) cause).getMessage());
                }
            }
            logger.error("All extensions are deactivated", e);
            ArrayList<DomainObject> toDeactivate = new ArrayList<>(activeExtensions.size());
            for (TagInfo extension : activeExtensions) {
                extension.extDomainObject.setBoolean("active", false);
                toDeactivate.add(extension.extDomainObject);
                domainObjectDao.save(toDeactivate, getSystemAccessToken());
            }
        }
    }

    private List<DomainObject> getAllConfigExtensionDomainObjects() {
        return domainObjectDao.findAll("configuration_extension", getSystemAccessToken());
    }

    private AccessToken getSystemAccessToken() {
        return accessControlService.createSystemAccessToken(this.getClass().getName());
    }

    private String getTagDefString(String tagType, String tagName) {
        return "Extension tag <" + tagType + "name=\"" + tagName + "\">";
    }

    private Map<String, TagTypeInfo> getTagClassMapping() {
        final Set<Class<?>> topLevelConfigClasses = configurationExplorer.getTopLevelConfigClasses();
        HashMap<String, TagTypeInfo> mapping = new HashMap<>(topLevelConfigClasses.size() * 3 / 2);
        for (Class clazz : topLevelConfigClasses) {
            final String tagType = getTagType(clazz);
            if (mapping.containsKey(tagType)) {
                logger.error("Top level Tag Type is defined twice (possible in different namespaces: " + tagType);
            }
            mapping.put(tagType, new TagTypeInfo(clazz));
        }
        return mapping;
    }

    private static String getTagType(Class<? extends TopLevelConfig> clazz) {
        return clazz.getAnnotation(Root.class).name();
    }

    private Configuration getCopy(Configuration configuration) {
        final ArrayList<TopLevelConfig> topLevelConfigs = new ArrayList<>(configuration.getConfigurationList());
        Configuration copy = new Configuration();
        copy.setConfigurationList(topLevelConfigs);
        return copy;
    }

    private static class TagInfo {
        public final Class<? extends TopLevelConfig> clazz;
        public final String tagType;
        public final String name;
        public final TopLevelConfig topLevelConfig;
        public final DomainObject extDomainObject;

        public TagInfo(String tagType, String name) {
            this.tagType = tagType;
            this.name = name;
            this.clazz = null;
            this.topLevelConfig = null;
            this.extDomainObject = null;
        }

        public TagInfo(TopLevelConfig topLevelConfig, DomainObject extDomainObject) {
            this.topLevelConfig = topLevelConfig;
            this.clazz = topLevelConfig.getClass();
            this.tagType = this.clazz.getAnnotation(Root.class).name();
            this.name = topLevelConfig.getName();
            this.extDomainObject = extDomainObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TagInfo tagInfo = (TagInfo) o;

            if (!tagType.equals(tagInfo.tagType)) return false;
            if (!name.equals(tagInfo.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class TagTypeInfo {
        public final Class<? extends TopLevelConfig> clazz;
        public TopLevelConfig.ExtensionPolicy creationPolicy;

        public TagTypeInfo(Class<? extends TopLevelConfig> clazz) {
            this.clazz = clazz;
            try {
                final TopLevelConfig config = clazz.newInstance();
                creationPolicy = config.getCreationPolicy();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Can't instantiate class: " + clazz, e);
                creationPolicy = TopLevelConfig.ExtensionPolicy.None;
            }
        }
    }
}
