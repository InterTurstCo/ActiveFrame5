package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 *         Date: 23.09.13
 *         Time: 20:21
 */
public class FormPluginConfig extends PluginConfig {

    private Id domainObjectId;
    private String domainObjectTypeToCreate;
    private String linkedFormName;
    private FormPluginState pluginState;
    private String domainObjectUpdatorComponent;
    private Dto updaterContext;
    private FormViewerConfig formViewerConfig;
    private FormState parentFormState;
    private Id parentId;
    public FormPluginConfig() {
    }

    public FormPluginConfig(Id domainObjectId) {
        this.domainObjectId = domainObjectId;
    }

    public FormPluginConfig(String domainObjectTypeToCreate) {
        this.domainObjectTypeToCreate = domainObjectTypeToCreate;
    }

    public FormPluginConfig(String domainObjectTypeToCreate, String linkedFormName) {
        this.domainObjectTypeToCreate = domainObjectTypeToCreate;
        this.linkedFormName = linkedFormName;
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

    public String getLinkedFormName() {
        return linkedFormName;
    }

    public void setLinkedFormName(String linkedFormName) {
        this.linkedFormName = linkedFormName;
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

    public FormViewerConfig getFormViewerConfig() {
        return formViewerConfig;
    }

    public void setFormViewerConfig(FormViewerConfig formViewerConfig) {
        this.formViewerConfig = formViewerConfig;
    }

    public Id getParentId() {
        return parentId;
    }

    public void setParentId(Id parentId) {
        this.parentId = parentId;
    }

    public FormState getParentFormState() {
        return parentFormState;
    }

    public void setParentFormState(FormState parentFormState) {
        this.parentFormState = parentFormState;
    }

    @Override
    public String getComponentName() {
        return "form.plugin";
    }

}
