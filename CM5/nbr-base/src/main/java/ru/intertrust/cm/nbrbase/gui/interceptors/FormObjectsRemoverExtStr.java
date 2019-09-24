package ru.intertrust.cm.nbrbase.gui.interceptors;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.form.FormObjectsRemover;

import java.util.List;

/**
 * Расширение для {@linkplain FormObjectsRemover} поддерживающее удаление
 * нескольких объектов
 * 
 * @author Ivan Fedosov
 *
 */
public interface FormObjectsRemoverExtStr extends FormObjectsRemoverExt {

    /**
     * Удаляет объекты по переданным идентификаторам
     * 
     * @param objectsIds
     *            идентификаторы объектов которые необходимо удалить
     * @return количество удаленных объектов
     */
    String deleteObj(List<Id> objectsIds);
}
