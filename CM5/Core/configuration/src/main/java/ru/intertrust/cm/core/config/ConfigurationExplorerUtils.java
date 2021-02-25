package ru.intertrust.cm.core.config;

import java.util.List;

/**
 * Класс с дополнительными методами для работы с конфигурациями, вынесенными сюда для того,
 * чтобы избежать расширения интерфейса ConfigurationExplorer, и как следствие, необходимости правок cmj.
 */
public abstract class ConfigurationExplorerUtils {
    /**
     * Полуение конфигурации вложений с учетом наследования доменного объекта
     * @param configurationExplorer экземпляр менеджера конфигураций
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return конфигурация вложений
     */
    public static AttachmentTypesConfig getAttachmentTypesConfigWithInherit(ConfigurationExplorer configurationExplorer,
                                                                            DomainObjectTypeConfig domainObjectTypeConfig) {
        AttachmentTypesConfig resultConfig = new AttachmentTypesConfig();
        if (configurationExplorer != null) {
            while (domainObjectTypeConfig != null) {
                AttachmentTypesConfig attachmentTypesConfig = domainObjectTypeConfig.getAttachmentTypesConfig();
                List<AttachmentTypeConfig> attachmentTypeConfigs = attachmentTypesConfig != null ?
                        attachmentTypesConfig.getAttachmentTypeConfigs() : null;
                if (attachmentTypeConfigs != null) {
                    resultConfig.getAttachmentTypeConfigs().addAll(attachmentTypeConfigs);
                }
                String parentTypeName = domainObjectTypeConfig.getExtendsAttribute();
                domainObjectTypeConfig = parentTypeName != null ? configurationExplorer.getDomainObjectTypeConfig(parentTypeName) : null;
            }
        }
        return resultConfig;
    }

    /**
     * Полуение конфигурации вложений с учетом наследования доменного объекта
     * @param configurationExplorer экземпляр менеджера конфигураций
     * @param domainObjectTypeName имя типа доменного объекта
     * @return конфигурация вложений
     */
    public static AttachmentTypesConfig getAttachmentTypesConfigWithInherit(ConfigurationExplorer configurationExplorer,
                                                                     String domainObjectTypeName) {

        return getAttachmentTypesConfigWithInherit(configurationExplorer,
                configurationExplorer != null ? configurationExplorer.getDomainObjectTypeConfig(domainObjectTypeName) : null);
    }
}
