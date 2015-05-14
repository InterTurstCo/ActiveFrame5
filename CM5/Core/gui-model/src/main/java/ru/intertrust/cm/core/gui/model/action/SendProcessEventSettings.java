package ru.intertrust.cm.core.gui.model.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.gui.action.ActionSettings;

@Root(name="send-process-event-action-settings")
public class SendProcessEventSettings implements ActionSettings {
    private static final long serialVersionUID = 708487097773208128L;

    /**
     * имя процесса
     */
    @Attribute(required=true, name="process-name")
    private String processName;

    /**
     * сообщение
     */
    @Attribute(required=true, name="event")
    private String event;
    
    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public Class<?> getActionContextClass() {
        return SendProcessEventActionContext.class;
    }
}
