package ru.intertrust.cm.nbrbase.gui.widget.renderers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.widget.LabelRenderer;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.text.SimpleDateFormat;
import java.util.Date;

@ComponentName("cmi.updatedby.info.renderer")
public class UpdatedByInfo extends LabelRenderer {

    @Autowired
    CrudService crudService;

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {

        final DomainObject msg = context.getFormObjects().getRootDomainObject();

        String infoStr = "Изменен: ";

        Id objLinkId = msg.getReference("updated_by");
        Date dateInfo = msg.getModifiedDate();

        if (dateInfo != null) {
            final SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");
            infoStr += f.format(dateInfo) + " - ";
        } else {
            infoStr += " ";
        }

        if (objLinkId == null || !this.crudService.exists(objLinkId))
            return infoStr + "Система";

        final DomainObject objMsg = this.crudService.find(objLinkId);

        String tStr = objMsg.getString("lastname");
        if (tStr!=null)
            infoStr += tStr + " ";
        tStr = objMsg.getString("firstname");
        if (tStr!=null)
            infoStr += tStr + " ";

        return infoStr;
    }
}
