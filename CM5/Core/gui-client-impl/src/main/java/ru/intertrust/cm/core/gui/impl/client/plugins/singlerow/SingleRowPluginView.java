package ru.intertrust.cm.core.gui.impl.client.plugins.singlerow;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;

import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

public class SingleRowPluginView extends PluginView{

    protected SingleRowPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IsWidget getViewWidget() {
        SingleRowPlugin singleRowPlugin = (SingleRowPlugin)plugin;
        
        Panel rootPanel = new AbsolutePanel();

        final PluginPanel formPluginPanel = new PluginPanel();
        formPluginPanel.open(singleRowPlugin.getFormPlugin());
        
        rootPanel.add(formPluginPanel);
        return rootPanel;
    }
}
