package ru.intertrust.cm.nbrbase.gui.interceptors;

import java.util.List;

import ru.intertrust.cm.core.gui.api.server.form.FormObjectsRemover;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Расширение для {@linkplain FormObjectsRemover} поддерживающее удаление
 * нескольких объектов
 * 
 * @author Ivan Fedosov
 *
 */
public interface FormObjectsRemoverExt extends FormObjectsRemover {
    /**
     * Удаляет объекты по переданным идентификаторам
     *
     * @param objectsIds
     *            идентификаторы объектов которые необходимо удалить
     * @return Сообщение
     */
    int deleteObjects(List<Id> objectsIds);
}
