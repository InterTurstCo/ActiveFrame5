package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Данный интерфейс описывает API для вызова различных методов управления строками
 * коллекций извне. Выделение, поиск фильтрация и прочее
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 28.06.2016
 * Time: 11:03
 * To change this template use File | Settings | File and Code Templates.
 */
public interface CollectionRemoteManagement {
    /**
     * Выделить строку с нужным Id обьекта.
     * Если строки нет в текущем списке, догрузить список.
     * @param objectId
     * @return false - если строка не найдена иначе true
     */
    void selectRowById(Id objectId);
}
