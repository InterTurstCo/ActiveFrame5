package ru.intertrust.cm.core.config.model;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.ElementList;

public class ActionSettingsConfig implements Serializable{

    @ElementListUnion({
        @ElementList(entry="start-process-action-settings", inline=true, type=StartProcessActionConfig.class),
        @ElementList(entry="complete-task-action-settings", inline=true, type=CompleteTaskActionData.class),
        @ElementList(entry="send-process-action-settings", inline=true, type=SendProcessEvent.class)
     })
     private List<ActionSettings> action;

    public List<ActionSettings> getAction() {
        return action;
    }

    public void setAction(List<ActionSettings> action) {
        this.action = action;
    }

}
