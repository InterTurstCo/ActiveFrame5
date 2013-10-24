package ru.intertrust.cm.core.config.model.gui;

import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name="process-action")
public interface ActionSettings extends Dto{

    /**
     * Возвращает название процесса
     * @return название процесса
     */
    String getProcessName();

    public void setProcessName(String processName) ;

    /**
     * Возвращает имя класса экшена
     * @return имя класса экшена
     */
    public String getClassName();

    public void setClassName(String className);

}
