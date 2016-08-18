package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class ExecutePluginData implements Dto {
    private String pluginId;
    private String parameter;
    public String getPluginId() {
        return pluginId;
    }
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    public String getParameter() {
        return parameter;
    }
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
    
    
}
