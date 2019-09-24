package ru.intertrust.cm.nbrbase.gui.widget.renderers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

@ComponentName("cmi.log.event.ip.renderer")
public class LogEventIPRenderer extends LabelRenderer {

    @Autowired
    CrudService crudService;

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {

        final DomainObject log = context.getFormObjects().getRootDomainObject();

        if (log == null)
            return "";

        final String evType = log.getString("event_type");

        if (evType == null || evType.isEmpty() || evType.equals("CLEAR_EVENT_LOG")) {
            return "";
        }

        return log.getString("client_ip_address");
    }
}
