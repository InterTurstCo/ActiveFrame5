package ru.intertrust.cm.core.business.api.plugin;

public interface PluginAutostartService {
    
    public interface Remote extends PluginAutostartService{
        
    }
    String execute(String id, String param);
}
