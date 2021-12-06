package ru.intertrust.cm.core.gui.impl.client;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.EmptyFormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

@ComponentName("empty.form.plugin")
public class EmptyFormPlugin extends FormPlugin {

    public EmptyFormPlugin() {
        showBreadcrumbs = false;
    }

    @Override
    public PluginView createView() {
        FormPluginData initialData = getInitialData();
        return new EmptyFormPluginView(this, initialData.getFormDisplayData());
    }

    @Override
    public EmptyFormPlugin createNew() {
        return new EmptyFormPlugin();
    }

    @Override
    public FormState getCurrentState() {
        return getFormState(null, false);
    }

    @Override
    protected void afterInitialDataChange(PluginData oldData, PluginData newData) {
        super.afterInitialDataChange(oldData, newData);
        ((EmptyFormPluginView) getView()).update(((FormPluginData) newData).getFormDisplayData().getFormState());
    }

    @Override
    public DomainObject getRootDomainObject() {
        return null;
        // this.<FormPluginData>getInitialData().getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject();
    }

    @Override
    public void replaceForm(FormPluginConfig formPluginConfig) {
        final EmptyFormPlugin newPlugin = ComponentRegistry.instance.get("empty.form.plugin");
        newPlugin.setConfig(formPluginConfig);
        getOwner().open(newPlugin);
        newPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
                                             @Override
                                             public void onViewCreation(PluginViewCreatedEvent source) {
                                                 eventBus.fireEvent(new PluginPanelSizeChangedEvent());
                                             }
                                         }
        );
    }

    @Override
    public void setFormState(FormState formState) {
        ((EmptyFormPluginView) getView()).update(formState);
        FormPluginData initialData = getInitialData();
        initialData.getFormDisplayData().setFormState(formState);
    }

    @Override
    public FormViewerConfig getFormViewerConfig() {
        return ((EmptyFormPluginConfig) this.getConfig()).getFormViewerConfig();
    }
}
