package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 23.09.13
 *         Time: 20:21
 */
public class FormPluginConfig extends PluginConfig {
    private Id domainObjectId;
    private String domainObjectTypeToCreate;
    private FormPluginState pluginState;
    private String domainObjectUpdatorComponent;
    private Dto updaterContext;

    public FormPluginConfig() {
    }

    public FormPluginConfig(Id domainObjectId) {
        this.domainObjectId = domainObjectId;
    }

    public FormPluginConfig(String domainObjectTypeToCreate) {
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

    public FormPluginState getPluginState() {
        if (pluginState == null) {
            pluginState = new FormPluginState();
        }
        return pluginState;
    }

    public void setPluginState(FormPluginState pluginState) {
        this.pluginState = pluginState;
    }

    public String getDomainObjectUpdatorComponent() {
        return domainObjectUpdatorComponent;
    }

    public void setDomainObjectUpdatorComponent(String domainObjectUpdatorComponent) {
        this.domainObjectUpdatorComponent = domainObjectUpdatorComponent;
    }

    public Dto getUpdaterContext() {
        return updaterContext;
    }

    public void setUpdaterContext(Dto updaterContext) {
        this.updaterContext = updaterContext;
    }

    @Override
    public String getComponentName() {
        return "form.plugin";
    }
}
