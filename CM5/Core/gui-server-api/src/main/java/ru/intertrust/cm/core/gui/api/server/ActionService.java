package ru.intertrust.cm.core.gui.api.server;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * Данный сервис отдает доступные действия для текущего контекста (типа доменного объекта и 
 * его статуса). 
 */
public interface ActionService {

    /**
     * Возвращает действия для переданного доменного объекта.
     * @return действия для переданного доменного объекта.
     */
    List<ActionContext>getActions(DomainObject domainObject); 
}
