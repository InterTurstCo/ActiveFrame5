package ru.intertrust.cm.core.dao.impl.attach;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.AttachmentTypeConfig;
import ru.intertrust.cm.core.config.AttachmentTypesConfig;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.UseAttachmentStorageConfig;

public class AttachmentStorageConfigHelper {

    public static final String DEFAULT_STORAGE = "default";

    @Autowired
    private ConfigurationExplorer confExplorer;
    //@Autowired private DomainObjectTypeIdCache typeIdCache;

    private final HashMap<Pair<String, String>, String> cache = new HashMap<>();
    //private HashMap<String, String> parentTypeCache = new HashMap<>();

    public String getStorageForAttachment(String attachmentType, String documentType) {
        Pair<String, String> key = new Pair<String, String>(attachmentType, documentType);
        String result = cache.get(key);
        if (result != null) {
            return result;
        }
        DomainObjectTypeConfig doConfig = confExplorer.getDomainObjectTypeConfig(documentType);
        String template = null;
        while (true) {
            // 1. Storage specified for exact type has the preference
            AttachmentTypesConfig attConfigs = doConfig.getAttachmentTypesConfig();
            if (attConfigs != null) {
                for (AttachmentTypeConfig attConfig : attConfigs.getAttachmentTypeConfigs()) {
                    if (attachmentType.equalsIgnoreCase(attConfig.getName())) {
                        String storageName = attConfig.getStorage();
                        if (storageName != null) {
                            cache.put(key, storageName);
                            return storageName;
                        }
                        // *) Remember attachment's template for possible use at step 4
                        if (template == null) {
                            template = attConfig.getTemplate();
                        }
                        break;
                    }
                }
            }
            // 2. Storage can be specified for all attachments in the type
            UseAttachmentStorageConfig useStorageConfig = doConfig.getAttachmentStorageConfig();
            if (useStorageConfig != null) {
                cache.put(key, useStorageConfig.getName());
                return useStorageConfig.getName();
            }
            // 3. Check object type's parents until root
            String parentType = doConfig.getExtendsAttribute();
            if (parentType == null) {
                break;
            }
            doConfig = confExplorer.getDomainObjectTypeConfig(parentType);
        }
        if (template == null) {
            template = "Attachment";    // *****
        }
        // 4. Storage can be specified in attachment template type
        doConfig = confExplorer.getDomainObjectTypeConfig(template);
        if (!doConfig.isTemplate()) {
            throw new ConfigurationException("Not an attachment template type: " + template);
        }
        UseAttachmentStorageConfig useStorageConfig = doConfig.getAttachmentStorageConfig();
        if (useStorageConfig != null) {
            cache.put(key, useStorageConfig.getName());
            return useStorageConfig.getName();
        }
        // 5. Use a default storage
        cache.put(key, DEFAULT_STORAGE);
        return DEFAULT_STORAGE;
    }
/*
    private AttachmentStorageTypeConfig fetchAndCacheStorageConfig(String name, Pair<String, String> key) {
        AttachmentStorageTypeConfig config = confExplorer.getConfig(AttachmentStorageTypeConfig.class, name);
        cache.put(key, config);
        return config;
    }

    public String getAttachmentParentRefFieldName(String attachmentType) {
        if (!confExplorer.isAttachmentType(attachmentType)) {
            throw new IllegalArgumentException(attachmentType + " is not an attachment type");
        }
        String parentType = parentTypeCache.get(attachmentType);
        if (parentType != null) {
            return parentType;
        }
        DomainObjectTypeConfig attConfig = confExplorer.getDomainObjectTypeConfig(attachmentType);
        ReferenceFieldConfig parentRefConfig = null;
        for (FieldConfig fieldConfig : attConfig.getFieldConfigs()) {
            if (!(fieldConfig instanceof ReferenceFieldConfig)) {
                continue;
            }
            parentRefConfig = (ReferenceFieldConfig) fieldConfig;
            DomainObjectTypeConfig typeConfig = confExplorer.getDomainObjectTypeConfig(parentRefConfig.getType());
            for (AttachmentTypeConfig attRefConfig : typeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                if (attachmentType.equalsIgnoreCase(attRefConfig.getName())) {
                    parentTypeCache.put(attachmentType, parentRefConfig.getName());
                    return parentRefConfig.getName();
                }
            }
        }
        throw new IllegalStateException(attachmentType + " has wrong configuration");
    }
*/
}
