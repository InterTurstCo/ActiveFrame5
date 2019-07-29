package ru.intertrust.cm.nbrbase.gui.widget.renderers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.HashMap;
import java.util.Map;

@ComponentName("person.is.active.render")
public class PersonIsActiveRenderer extends LabelRenderer {

    @Autowired
    CrudService crudService;

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {

        final DomainObject person = context.getFormObjects().getRootDomainObject();
        Id status = person.getReference("status");
        if (status != null){
            boolean isEditable = GuiContext.get().getFormPluginState().isEditable();
            Map<String,Value> map = new HashMap<>();
            map.put("name", new StringValue("Sleep"));
            DomainObject statusSleep = crudService.findByUniqueKey("status",map);
            if(status.equals(statusSleep.getId()) && !isEditable){
                return "Не активен";
            }
        }
        return "";
    }
}
