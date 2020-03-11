package ru.intertrust.cm.core.gui.impl.client.plugins.singlerow;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.SingleRowPluginData;

@ComponentName("single.row.plugin")
public class SingleRowPlugin extends Plugin {

    private FormPlugin formPlugin;
    
    // локальная шина событий
    private EventBus eventBus;    

    public SingleRowPlugin() {
        // устанавливается локальная шина событий
        eventBus = GWT.create(SimpleEventBus.class);
    }
    
    @Override
    public Component createNew() {
        return new SingleRowPlugin();
    }

    @Override
    public PluginView createView() {
        return new SingleRowPluginView(this);
    }

    public FormPlugin getFormPlugin() {
        return formPlugin;
    }

    @Override
    public void setInitialData(PluginData inData) {
        super.setInitialData(inData);
        SingleRowPluginData initialData = (SingleRowPluginData) inData;

        if (formPlugin == null) {
            formPlugin = ComponentRegistry.instance.get("form.plugin");
            formPlugin.setDisplayActionToolBar(true);
            formPlugin.setLocalEventBus(this.eventBus);

            FormPluginConfig formPluginConfig = new FormPluginConfig();
            if (initialData.getFormPluginData().getFormDisplayData().getStatus() != null){
                formPluginConfig.setDomainObjectId(initialData.getFormPluginData().getFormDisplayData().getStatus().getId());
            }else {
                formPluginConfig.setDomainObjectTypeToCreate(initialData.getFormPluginData().getFormDisplayData().getFormState().getRootDomainObjectType());                
            }
            formPlugin.setConfig(formPluginConfig);
        }
        formPlugin.setInitialData(initialData.getFormPluginData());
        formPlugin.getFormPluginState().setInCentralPanel(true);
    }

}
