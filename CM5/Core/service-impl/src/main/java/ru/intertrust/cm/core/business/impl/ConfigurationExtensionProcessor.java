package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.event.ConfigChange;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.io.File;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 20.04.2017
 *         Time: 13:33
 */
public class ConfigurationExtensionProcessor {
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationExtensionProcessor.class);
    public static final String CONFIGURATION_START = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<configuration xmlns=\"https://cm5.intertrust.ru/config\" xmlns:act=\"https://cm5.intertrust.ru/config/action\">";
    public static final String CONFIGURATION_END = "</configuration>";
    private static final Object GLOBAL_LOCK = new Object();
    
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
    @Autowired
    private ConfigurationExtensionHelper configurationExtensionHelper;

    @org.springframework.beans.factory.annotation.Value("${NEVER.USE.IN.PRODUCTION.dev.mode.configuration.update:false}")
    private boolean useDevModeConfigUpdate;

    // this bean is prototype, it is its local cache
    public Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelDistributiveConfigs = new HashMap<>();

    private AccessToken accessToken;

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyConfigurationExtensionCleaningOutInvalid() {
        synchronized (GLOBAL_LOCK) {
            final ExtensionsInfo extensions = getExtensionsInformation();
            cleanOutInvalid(extensions);
            createNewInactiveDistributiveExtensions();
            try {
                applyNewExplorer(getNewExplorer(extensions.getValidActiveExtensions()));
            } catch (Throwable e) {
                Throwable cause = null;
                if (e instanceof ConfigurationException) {
                    cause = e;
                } else if (e instanceof FatalBeanException) {
                    cause = ((FatalBeanException) e).getCause();
                }
                if (cause != null && cause instanceof ConfigurationException) {
                    logger.error("Configuration extension not applied. List of errors:");
                    logger.error(((ConfigurationException) cause).getMessage());
                }
                logger.error("All extensions are deactivated", e);
                deactivateExtensions(extensions.getValidActiveExtensions());
            }
        }
    }

    public Set<ConfigChange> applyConfigurationExtension() {
        synchronized (GLOBAL_LOCK) {
            final ExtensionsInfo extensions = getExtensionsInformation();
            return applyNewExplorer(getNewExplorer(extensions.getValidActiveExtensions()));
        }
    }

    public Set<ConfigChange> activateDraftsById(List<Id> toolingIds) {
        synchronized (GLOBAL_LOCK) {
            return activateDrafts(domainObjectDao.find(new ArrayList<>(toolingIds), accessToken));
        }
    }

    public Set<ConfigChange> activateDrafts(List<DomainObject> toolingDOs) {
        return validateAndActivateDrafts(toolingDOs);
    }

    public Set<ConfigChange> validateAndActivateDrafts(List<DomainObject> toolingDOs) {
        synchronized (GLOBAL_LOCK) {
            copyDraftsToExtensions(toolingDOs);
            final ExtensionsInfo extensions = getExtensionsInformation();
            if (!extensions.isValid()) {
                throw new SummaryConfigurationException(extensions.getAllProblems());
            }
            final ConfigurationExplorer newExplorer = getNewExplorer(extensions.getValidActiveExtensions());
            return applyNewExplorer(newExplorer);
        }
    }

    public void validateDrafts(List<DomainObject> toolingDOs) {
        final ArrayList<ConfigurationException> result = new ArrayList<>();
        synchronized (GLOBAL_LOCK) {
            copyDraftsToExtensions(toolingDOs);
            final ExtensionsInfo extensions = getExtensionsInformation();
            if (!extensions.isValid()) {
                result.addAll(extensions.getAllProblems());
            }
            try {
                final ConfigurationExplorer newExplorer = getNewExplorer(extensions.getValidActiveExtensions());
            } catch (ConfigurationException e) {
                result.add(e);
            }
            if (!result.isEmpty()) {
                throw new SummaryConfigurationException(result);
            }
        }
    }

    public void copyDraftsToExtensions(List<DomainObject> toolingDOs) {
        for (DomainObject toolingDO : toolingDOs) {
            final Id extensionId = toolingDO.getReference("configuration_extension");
            DomainObject extensionDO;
            if (extensionId == null) {
                extensionDO = new GenericDomainObject("configuration_extension");
                TopLevelConfig draftConfig = parseXML(toolingDO.getString("draft_xml"));
                extensionDO.setString("type", getTagType(draftConfig.getClass()));
                extensionDO.setString("name", draftConfig.getName());
            } else{
                extensionDO = domainObjectDao.find(extensionId, accessToken);
            }
            extensionDO.setString("current_xml", toolingDO.getString("draft_xml"));
            extensionDO.setBoolean("active", true);
            extensionDO = domainObjectDao.save(extensionDO, accessToken);
            if (extensionId == null) {
                toolingDO.setReference("configuration_extension", extensionDO.getId());
                domainObjectDao.save(toolingDO, accessToken);
            }
        }
    }

    public void saveDrafts(List<DomainObject> toolingDOs) throws ConfigurationException {
        synchronized (GLOBAL_LOCK) {
            for (DomainObject toolingDO : toolingDOs) {
                final Id extensionId = toolingDO.getReference("configuration_extension");
                if (toolingDO.isNew()) {
                    if (extensionId != null) {
                        throw new ConfigurationException("New draft should not reference extension");
                    }
                    TopLevelConfig draftConfig = parseXML(toolingDO.getString("draft_xml"));
                    DomainObject extensionDO = new GenericDomainObject("configuration_extension");
                    extensionDO.setString("type", getTagType(draftConfig.getClass()));
                    extensionDO.setString("name", draftConfig.getName());
                    extensionDO.setString("current_xml", null);
                    extensionDO.setBoolean("active", false);
                    extensionDO = domainObjectDao.save(extensionDO, accessToken);
                    toolingDO.setReference("configuration_extension", extensionDO.getId());
                }
                domainObjectDao.save(toolingDO, accessToken);
            }
        }
    }

    public Set<ConfigChange> activateDrafts() {
        synchronized (GLOBAL_LOCK) {
            return activateDrafts(domainObjectDao.findAll("config_extension_tooling", accessToken));
        }
    }

    public Set<ConfigChange> activateFromFiles(Collection<File> files) {
        synchronized (GLOBAL_LOCK) {
            final Configuration configuration = configurationSerializer.deserializeConfiguration(files);
            for (TopLevelConfig draftConfig : configuration.getConfigurationList()) {
                final String configType = getTagType(draftConfig.getClass());
                final String configName = draftConfig.getName();
                final HashMap<String, Value> map = new HashMap<>();
                map.put("type", new StringValue(configType));
                map.put("name", new StringValue(configName));
                DomainObject extensionDO = domainObjectDao.findByUniqueKey("configuration_extension", map, accessToken);
                if (extensionDO == null) {
                    extensionDO = new GenericDomainObject("configuration_extension");
                    extensionDO.setString("type", configType);
                    extensionDO.setString("name", configName);
                }
                extensionDO.setString("current_xml", ConfigurationSerializer.serializeConfiguration(draftConfig));
                extensionDO.setBoolean("active", true);
                domainObjectDao.save(extensionDO, accessToken);
            }
            final ExtensionsInfo extensions = getExtensionsInformation();
            cleanOutInvalid(extensions);
            return applyNewExplorer(getNewExplorer(extensions.getValidActiveExtensions()));
        }
    }

    private ExtensionsInfo getExtensionsInformation() {
        final List<DomainObject> extensionDOs = getAllConfigExtensionDomainObjects();
        final Map<String, ConfigurationExtensionHelper.TagTypeInfo> tagTypeByTagName = configurationExtensionHelper.getTagClassMapping();

        ExtensionsInfo extensionsInfo = new ExtensionsInfo();

        // get replaced and remove if they are not replaceable anymore
        // get "new" (not replaced) and remove if it's not allowed to create new anymore
        for (DomainObject extensionDO : extensionDOs) {
            final String tagType = extensionDO.getString("type");
            final String tagName = extensionDO.getString("name");
            final ConfigurationExtensionHelper.TagTypeInfo tagTypeInfo = tagTypeByTagName.get(tagType);
            if (tagTypeInfo == null) {
                extensionsInfo.addToDelete(extensionDO, new ConfigurationException(getTagDefString(extensionDO) + " deleted - tag unknown"));
                continue;
            }
            final TopLevelConfig distrConfig = configurationExtensionHelper.getDistributiveConfig(tagTypeInfo.getTopLevelConfigClass(), tagName);
            if (distrConfig != null) {
                if (distrConfig.getReplacementPolicy() != TopLevelConfig.ExtensionPolicy.Runtime) {
                    extensionsInfo.addToDelete(extensionDO, new ConfigurationException(getTagDefString(extensionDO) + " deleted - tag is not replaceable"));
                    continue;
                }
            } else if (tagTypeInfo.getCreationPolicy() != TopLevelConfig.ExtensionPolicy.Runtime) {
                extensionsInfo.addToDelete(extensionDO, new ConfigurationException(getTagDefString(extensionDO) + " deleted - new tags of this type are not allowed"));
            }

            final boolean active = extensionDO.getBoolean("active");
            if (!active) {
                continue;
            }
            final String overriddenXml = extensionDO.getString("current_xml");
            TopLevelConfig extensionConfig = null;
            try {
                extensionConfig = parseXML(overriddenXml);
                if (!extensionConfig.getClass().equals(tagTypeInfo.getTopLevelConfigClass())) {
                    extensionsInfo.addToDeactivateAndClearXML(extensionDO, new ConfigurationException("XML doesn't match the tag: " + overriddenXml));
                } else if (!extensionConfig.getName().equals(tagName)) {
                    extensionsInfo.addToDeactivate(extensionDO, new ConfigurationException("XML doesn't match name: " + overriddenXml));
                } else {
                    extensionsInfo.addValid(new TagInfo(extensionConfig, extensionDO));
                }
            } catch (DeactivationException e) {
                extensionsInfo.addToDeactivate(extensionDO, (ConfigurationException) e.getCause());
                continue;
            } catch (DeactivationWithCleaningException e) {
                extensionsInfo.addToDeactivateAndClearXML(extensionDO, (ConfigurationException) e.getCause());
                continue;
            }
        }
        return extensionsInfo;
    }

    private void cleanOutInvalid(ExtensionsInfo extensionsInfo) {
        ArrayList<DomainObject> toDeactivate = extensionsInfo.getToDeactivate();
        for (int i = 0; i < toDeactivate.size(); i++) {
            DomainObject domainObject = toDeactivate.get(i);
            domainObject.setBoolean("active", false);
            logger.warn(getTagDefString(domainObject) + " deactivated. " + extensionsInfo.getDeactivationReasons().get(i).getMessage());
        }
        ArrayList<DomainObject> toDeactivateAndClearXML = extensionsInfo.getToDeactivateAndClearXML();
        for (int i = 0; i < toDeactivateAndClearXML.size(); i++) {
            DomainObject domainObject = toDeactivateAndClearXML.get(i);
            domainObject.setBoolean("active", false);
            domainObject.setString("current_xml", null);
            logger.warn(getTagDefString(domainObject) + " deactivated and XML cleared. " + extensionsInfo.getDeactivationAndClearXMLReasons().get(i).getMessage());
        }
        final AccessToken systemAccessToken = accessToken;
        domainObjectDao.save(extensionsInfo.getToDeactivate(), systemAccessToken);
        domainObjectDao.save(extensionsInfo.getToDeactivateAndClearXML(), systemAccessToken);
        ArrayList<Id> toDelete = extensionsInfo.getToDelete();
        for (int i = 0; i < toDelete.size(); i++) {
            Id id = toDelete.get(i);
            final List<Id> linkedDomainObjects = domainObjectDao.findLinkedDomainObjectsIds(id, "config_extension_tooling", "configuration_extension", systemAccessToken);
            domainObjectDao.delete(linkedDomainObjects, systemAccessToken);
            logger.warn(getTagDefString(extensionsInfo.getToDeleteDomainObjects().get(i)) + " deactivated and XML cleared. " + extensionsInfo.getDeleteReasons().get(i).getMessage());
        }
        domainObjectDao.delete(extensionsInfo.getToDelete(), systemAccessToken);

    }

    private TopLevelConfig parseXML(String overriddenXml) {
        if (overriddenXml == null || overriddenXml.trim().isEmpty()) {
            throw new DeactivationException("Empty XML");
        }
        final StringBuilder configString = new StringBuilder(CONFIGURATION_START.length() + overriddenXml.length() + CONFIGURATION_END.length());
        configString.append(CONFIGURATION_START); // todo: XSD schemas should include custom schemas as well
        configString.append(overriddenXml);
        configString.append(CONFIGURATION_END);
        List<TopLevelConfig> configurationList = null;
        try {
            configurationList = configurationSerializer.deserializeConfiguration(configString.toString()).getConfigurationList();
        } catch (ConfigurationException e) {
            throw new DeactivationException("XML not valid", e);
        }
        if (configurationList.size() != 1) {
            throw new DeactivationWithCleaningException("XML doesn't define exactly 1 valid tag");
        }
        return configurationList.get(0);
    }

    private void createNewInactiveDistributiveExtensions() {
        final List<DomainObject> extensionDOs = getAllConfigExtensionDomainObjects();
        final Set<TagInfo> currentExtensions = new HashSet<>();
        for (DomainObject extensionDO : extensionDOs) {
            final String tagType = extensionDO.getString("type");
            final String tagName = extensionDO.getString("name");
            currentExtensions.add(new TagInfo(tagType, tagName));
        }
        final List<TopLevelConfig> distibutiveConfigs = configurationExplorer.getDistributiveConfiguration().getConfigurationList();
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
        domainObjectDao.save(newExtensibleTags, accessToken);
    }

    private ConfigurationExplorer getNewExplorer(Collection<TagInfo> activeExtensions) {
        HashMap<TagInfo, TagInfo> extensionsMap = new HashMap<>(activeExtensions.size() * 2);
        for (TagInfo activeExtension : activeExtensions) {
            extensionsMap.put(activeExtension, activeExtension);
        }
        final ArrayList<TopLevelConfig> configurationList = new ArrayList<>(configurationExplorer.getDistributiveConfiguration().getConfigurationList().size() + extensionsMap.size());
        for (TopLevelConfig topLevelConfig : configurationExplorer.getDistributiveConfiguration().getConfigurationList()) {
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

        return new ConfigurationExplorerImpl(configuration, context, false);
    }

    private Set<ConfigChange> applyNewExplorer(ConfigurationExplorer newExplorer) {
        return ((ConfigurationExplorerImpl) configurationExplorer).copyFrom((ConfigurationExplorerImpl) newExplorer);
    }

    private void deactivateExtensions(Collection<TagInfo> activeExtensions) {
        ArrayList<DomainObject> toDeactivate = new ArrayList<>(activeExtensions.size());
        for (TagInfo extension : activeExtensions) {
            extension.extDomainObject.setBoolean("active", false);
            toDeactivate.add(extension.extDomainObject);
            domainObjectDao.save(toDeactivate, accessToken);
        }
    }

    private List<DomainObject> getAllConfigExtensionDomainObjects() {
        return domainObjectDao.findAll("configuration_extension", accessToken);
    }

    private String getTagDefString(DomainObject extensionDO) {
        final String tagType = extensionDO.getString("type");
        final String tagName = extensionDO.getString("name");
        return "Extension tag <" + tagType + " name=\"" + tagName + "\">";
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

    private static class ExtensionsInfo {
        private ArrayList<TagInfo> validActiveExtensions = new ArrayList<>();
        private ArrayList<DomainObject> toDeactivate = new ArrayList<>();
        private ArrayList<DomainObject> toDeactivateAndClearXML = new ArrayList<>();
        private ArrayList<Id> toDelete = new ArrayList<>();
        private ArrayList<DomainObject> toDeleteDomainObjects = new ArrayList<>();
        private ArrayList<ConfigurationException> deactivationReasons = new ArrayList<>();
        private ArrayList<ConfigurationException> deactivationAndClearXMLReasons = new ArrayList<>();
        private ArrayList<ConfigurationException> deleteReasons = new ArrayList<>();

        public void addValid(TagInfo tagInfo) {
            validActiveExtensions.add(tagInfo);
        }

        public void addToDeactivate(DomainObject extension, ConfigurationException reason) {
            toDeactivate.add(extension);
            deactivationReasons.add(reason);
        }

        public void addToDeactivateAndClearXML(DomainObject extension, ConfigurationException reason) {
            toDeactivateAndClearXML.add(extension);
            deactivationAndClearXMLReasons.add(reason);
        }

        public void addToDelete(DomainObject extension, ConfigurationException reason) {
            toDeleteDomainObjects.add(extension);
            toDelete.add(extension.getId());
            deleteReasons.add(reason);
        }

        public ArrayList<TagInfo> getValidActiveExtensions() {
            return validActiveExtensions;
        }

        public ArrayList<DomainObject> getToDeactivate() {
            return toDeactivate;
        }

        public ArrayList<DomainObject> getToDeactivateAndClearXML() {
            return toDeactivateAndClearXML;
        }

        public ArrayList<Id> getToDelete() {
            return toDelete;
        }

        public ArrayList<DomainObject> getToDeleteDomainObjects() {
            return toDeleteDomainObjects;
        }

        public ArrayList<ConfigurationException> getDeactivationReasons() {
            return deactivationReasons;
        }

        public ArrayList<ConfigurationException> getDeactivationAndClearXMLReasons() {
            return deactivationAndClearXMLReasons;
        }

        public ArrayList<ConfigurationException> getDeleteReasons() {
            return deleteReasons;
        }

        public boolean isValid() {
            return toDeactivate.isEmpty() && toDelete.isEmpty() && toDeactivateAndClearXML.isEmpty();
        }

        public Collection<ConfigurationException> getAllProblems() {
            final ArrayList<ConfigurationException> result = new ArrayList<>();
            result.addAll(getDeactivationReasons());
            result.addAll(getDeactivationAndClearXMLReasons());
            result.addAll(getDeleteReasons());
            return result;
        }
    }

    private static class DeactivationException extends ConfigurationException {
        public DeactivationException() {
        }

        public DeactivationException(String message) {
            super(message);
        }

        public DeactivationException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeactivationException(Throwable cause) {
            super(cause);
        }

        public DeactivationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    private static class DeactivationWithCleaningException extends ConfigurationException {
        public DeactivationWithCleaningException() {
        }

        public DeactivationWithCleaningException(String message) {
            super(message);
        }

        public DeactivationWithCleaningException(String message, Throwable cause) {
            super(message, cause);
        }

        public DeactivationWithCleaningException(Throwable cause) {
            super(cause);
        }

        public DeactivationWithCleaningException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
