package ru.intertrust.cm.core.config.gui.action;

import ru.intertrust.cm.core.business.api.dto.Dto;

public interface ActionSettings extends Dto {

    /**
     * Возвращает класс который нужно создать в ActionSerevice.getActions() для передачи гуи.
     * Если метод возвращает NULL то создастся класс ActionContext
     * @return
     */
    Class<?> getActionContextClass();
}
