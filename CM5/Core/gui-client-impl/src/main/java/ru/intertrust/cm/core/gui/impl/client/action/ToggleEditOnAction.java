package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 */
@ComponentName("toggle.edit.on.action")
public class ToggleEditOnAction extends ToggleAction {

    @Override
    protected void execute() {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final Id id = editor.getFormState().getObjects().getRootNode().getDomainObject().getId();
        final FormPluginConfig config;
        if (id == null) {
            config = new FormPluginConfig(editor.getRootDomainObject().getTypeName());
        } else {
            config = new FormPluginConfig(id);
        }
        final FormPluginState state = editor.getFormPluginState().createClone();
        config.setPluginState(state);
        state.setEditable(true);
        config.setFormViewerConfig(editor.getFormViewerConfig());
        final FormPlugin formPlugin = createFormPlugin(config);
        final boolean isCentral = state.isInCentralPanel();
        if(!isCentral){
            state.setInCentralPanel(true);
        }
        PluginViewCreatedEventListener listener = new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                if (isCentral) {
                    getPlugin().getOwner().closeCurrentPlugin();
                }
            }
        };
        formPlugin.addViewCreatedListener(listener);
        Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));

    }

    @Override
    public Component createNew() {
        return new ToggleEditOnAction();
    }

    private FormPlugin createFormPlugin(final FormPluginConfig config) {
        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.setDisplayActionToolBar(true);
        formPlugin.setLocalEventBus(plugin.getLocalEventBus());
        return formPlugin;
    }
}
