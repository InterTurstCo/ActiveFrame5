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

}
