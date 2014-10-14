package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * Перехватчик точки расширения "после сохранением формы"
 * @author Denis Mitavskiy
 *         Date: 13.10.2014
 *         Time: 16:56
 */
public interface FormAfterSaveInterceptor extends ComponentHandler {
    /**
     * Метод вызываемый перед после сохранения формы. Если корневой доменный объект изменяется каким-то образом, то данные изменения
     * должны найти отражение в результирующем объекте.
     * @param rootDomainObject корневой доменный объект формы после её сохранения
     * @return доменный объект, возможно изменённый данным перехватчиком
     */
    DomainObject afterSave(DomainObject rootDomainObject);
}
