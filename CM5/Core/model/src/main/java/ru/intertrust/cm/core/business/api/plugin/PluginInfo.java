package ru.intertrust.cm.core.business.api.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class PluginInfo implements Dto{

    private static final long serialVersionUID = -7517649811163531202L;
    private String name;
    private String description;
    private String contextName;
    private boolean autostart;
    private String className;
    
    public PluginInfo(){
        
    }
    
    public PluginInfo(String className, String name, String description, String contextName, boolean autostart){
        this.setClassName(className);
        this.name = name;
        this.description = description;
        this.contextName = contextName;
        this.setAutostart(autostart);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }   
    
}
