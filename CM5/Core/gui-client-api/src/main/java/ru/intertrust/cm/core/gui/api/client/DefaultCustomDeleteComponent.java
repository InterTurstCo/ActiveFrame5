package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEvent;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 12:03
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("default.custom.delete.component")
public class DefaultCustomDeleteComponent implements CustomDelete {
    EventBus localEventBus = new SimpleEventBus();

    @Override
    public void delete(Id id) {
        Window.alert("Инициирую удаление...");
        localEventBus.fireEvent(new CustomDeleteEvent(CustomDeleteEventHandler.DeleteStatus.OK,"Успешно удалено..."));
    }
}
