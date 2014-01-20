package ru.intertrust.cm.core.gui.impl.client.action;


import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.SaveToCsvEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;


/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 11.01.14
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
@ComponentName("save-csv.action")
public class SaveToCsv extends Action {
    EventBus eventBus;

    @Override
    public Component createNew() {
        return new SaveToCsv();
    }

    @Override
    public void execute() {
        eventBus = plugin.getLocalEventBus();
        eventBus.fireEvent(new SaveToCsvEvent());

    }

}