package ru.intertrust.cm.core.business.api.plugin;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class PluginInfo implements Dto{

    private static final long serialVersionUID = -7517649811163531202L;
    private String name;
    private String description;
    private String contextName;
    private boolean autostart;
    private boolean transactional;
    private String className;
    private Status status;
    private boolean checked = false;
    private Date lastStart;
    private Date lastFinish;
    private Id lastResultId;
    
    public enum Status{
        Sleeping,
        Running,
        Terminating
    }
    
    public PluginInfo(){
        
    }
    
    public PluginInfo(String className, String name, String description, String contextName, boolean autostart, boolean transactional){
        this.setClassName(className);
        this.name = name;
        this.description = description;
        this.contextName = contextName;
        this.setAutostart(autostart);
        this.setTransactional(transactional);
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }    

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getLastStart() {
        return lastStart;
    }

    public void setLastStart(Date lastStart) {
        this.lastStart = lastStart;
    }

    public Date getLastFinish() {
        return lastFinish;
    }

    public void setLastFinish(Date lastFinish) {
        this.lastFinish = lastFinish;
    }

    public Id getLastResult() {
        return lastResultId;
    }

    public void setLastResult(Id lastResult) {
        this.lastResultId = lastResult;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }   
    
}
