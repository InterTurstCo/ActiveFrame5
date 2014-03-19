package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.GenerateReportEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 17:09
 */

@ComponentName("generate-report.action")
public class GenerateReportAction extends Action {

    EventBus eventBus;

    @Override
    public Component createNew() {
        return new GenerateReportAction();
    }

    @Override
    public void execute() {
        eventBus = plugin.getLocalEventBus();
        eventBus.fireEvent(new GenerateReportEvent());
    }
}
