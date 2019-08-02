package ru.intertrust.cm.nbrbase.gui.widget.renderers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

@ComponentName("cmi.log.event.type.renderer")
public class LogEventTypeRenderer extends LabelRenderer {

    @Autowired
    CrudService crudService;

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {

        final DomainObject msg = context.getFormObjects().getRootDomainObject();

        if (msg == null)
            return "Протокол аудита";

        final String evType = msg.getString("event_type");

        if (evType == null || evType.isEmpty())
            return "Протокол аудита";

        if ("LOGIN".equalsIgnoreCase(evType))
        {
            return "Протокол аудита: Вход в систему";
        }
        else if ("LOGOUT".equalsIgnoreCase(evType))
        {
            return "Протокол аудита: Выход из системы";
        }
        else if ("CLEAR_EVENT_LOG".equalsIgnoreCase(evType))
        {
            return "Протокол аудита: Удаление логов аудита";
        }

        return "Протокол аудита";
    }
}
