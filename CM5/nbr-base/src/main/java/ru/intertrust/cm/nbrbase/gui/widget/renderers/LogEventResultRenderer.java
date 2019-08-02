package ru.intertrust.cm.nbrbase.gui.widget.renderers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.business.api.dto.Id;

@ComponentName("cmi.log.event.result.renderer")
public class LogEventResultRenderer extends LabelRenderer {

    @Autowired
    CrudService crudService;

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {

        final DomainObject msg = context.getFormObjects().getRootDomainObject();

        if (msg == null)
            return "";

        final String evType = msg.getString("event_type");

        if (evType == null || evType.isEmpty()) {
            return "";
        }

        final Boolean resType = msg.getBoolean("success");
        final String userid = msg.getString("user_id");
        final Id personid = msg.getReference("person");

        if ("LOGIN".equalsIgnoreCase(evType) && personid == null && resType == true)
        {
            if (userid == null || userid.isEmpty())
                return "Не успешно";

            return "Не успешно ("+ userid+")";
        }

        return resType ? "Успешно" : "Не успешно";
    }
}
