package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.EmptyFormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

@ComponentName(EmptyFormPluginHandler.COMPONENT_NAME)
public class EmptyFormPluginHandler extends FormPluginHandler {
    static final String COMPONENT_NAME = "empty.form.plugin";

    @Override
    public FormPluginData initialize(Dto initialData) {
        final EmptyFormPluginConfig formPluginConfig = initialData != null ?
                (EmptyFormPluginConfig) initialData : new EmptyFormPluginConfig();
        GuiContext.get().setFormPluginState(formPluginConfig.getPluginState());
        FormDisplayData form = getFormDisplayData();
        formPluginConfig.getPluginState().setEditable(false);
        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        pluginData.setPluginState(formPluginConfig.getPluginState());
        ToolbarContext toolbarContext = getActionContexts();
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }

    private FormDisplayData getFormDisplayData() {
        return new FormDisplayData();
    }

    private ToolbarContext getActionContexts() {
        return getToolbarContexts();
    }

    private ToolbarContext getToolbarContexts() {
        return new ToolbarContext();
    }
}
