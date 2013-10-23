package ru.intertrust.cm.core.config.model.gui;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public interface ActionSettings extends Serializable{

    /**
     * Возвращает название процесса
     * @return название процесса
     */
    String getProcessName();
}
