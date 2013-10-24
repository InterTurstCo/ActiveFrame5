package ru.intertrust.cm.core.config.model.gui;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

public class ActionSettingsConfig {


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
