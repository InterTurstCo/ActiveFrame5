package ru.intertrust.cm.core.gui.model.plugin;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;

public class PluginInfoData implements Dto{
    private List<PluginInfo> pluginInfos = new ArrayList<PluginInfo>();

    public List<PluginInfo> getPluginInfos() {
        return pluginInfos;
    }

    public void setPluginInfos(List<PluginInfo> pluginInfos) {
        this.pluginInfos = pluginInfos;
    }
    
}
