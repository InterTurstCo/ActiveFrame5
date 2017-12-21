package ru.intertrust.cm.core.gui.model.plugin;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class TerminatePluginData implements Dto{
    private List<String> pluginIds = new ArrayList<String>();

    public List<String> getPluginIds() {
        return pluginIds;
    }

    public void setPluginIds(List<String> pluginIds) {
        this.pluginIds = pluginIds;
    }
    
    
}
