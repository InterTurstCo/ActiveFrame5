package ru.intertrust.cm.core.gui.impl.client.plugins.plugin;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginState;

@ComponentName("plugin.manager.plugin")
public class PluginManager extends Plugin implements IsActive{

    @Override
    public Component createNew() {
        return new PluginManager(); 
    }

    @Override
    public <E extends PluginState> E getPluginState() {
        return null;
    }

    @Override
    public void setPluginState(PluginState pluginState) {
    }

    @Override
    public PluginView createView() {        
        return new PluginManagerView(this);
    }
    
    @Override
    public void setInitialData(PluginData initialData) {
        super.setInitialData(initialData);
        setDisplayActionToolBar(true);
    }    

}
