package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class ActionSettingsConfig implements Dto {


    /**
     * Конфигурация экшенов
     */
    @Element(name="process-action")
    @Convert(ActionSettingsConverter.class)
    private ActionSettings processAction;


    public ActionSettings getProcessAction() {
        return processAction;
    }

    public void setProcessAction(ActionSettings processAction) {
        this.processAction = processAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionSettingsConfig that = (ActionSettingsConfig) o;

        if (processAction != null ? !processAction.equals(that.processAction) : that.processAction != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return processAction != null ? processAction.hashCode() : 0;
    }
}
