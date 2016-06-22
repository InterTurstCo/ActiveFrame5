package ru.intertrust.cm.core.gui.api.client;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Интерфейс кастомного компонента для удаления записей из таблиц
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 10:07
 * To change this template use File | Settings | File and Code Templates.
 */
public interface CustomDelete {

    void delete(Id id, EventBus eventBus);
}
