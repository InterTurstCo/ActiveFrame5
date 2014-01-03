package ru.intertrust.cm.core.gui.model.plugin;

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
    private FormPluginMode mode;
    private boolean editable;

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

    public FormPluginMode getMode() {
        return mode == null ? FormPluginMode.EDITABLE : mode;
    }

    public void setMode(final FormPluginMode mode) {
        this.mode = mode;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    @Override
    public String getComponentName() {
        return "form.plugin";
    }
}
