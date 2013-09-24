package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.gui.navigation.PluginConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 23.09.13
 *         Time: 20:21
 */
public class SomeActivePluginConfig extends PluginConfig {
    private Id domainObjectId;
    private String domainObjectTypeToCreate;

    public SomeActivePluginConfig() {
    }

    public SomeActivePluginConfig(Id domainObjectId) {
        this.domainObjectId = domainObjectId;
    }

    public SomeActivePluginConfig(String domainObjectTypeToCreate) {
        this.domainObjectTypeToCreate = domainObjectTypeToCreate;
    }

    public Id getDomainObjectId() {
        return domainObjectId;
    }

    public void setDomainObjectId(Id domainObjectId) {
        this.domainObjectId = domainObjectId;
    }

    public String getDomainObjectTypeToCreate() {
        return domainObjectTypeToCreate;
    }

    public void setDomainObjectTypeToCreate(String domainObjectTypeToCreate) {
        this.domainObjectTypeToCreate = domainObjectTypeToCreate;
    }
}
