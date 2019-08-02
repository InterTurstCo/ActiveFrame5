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

@ComponentName("nbrbase.createdby.updatedby.info.renderer")
public class CreatedUpdatedByInfo extends LabelRenderer {

    @Autowired
    CrudService crudService;

    @Override
    public String composeString(FieldPath[] fieldPaths, WidgetContext context) {

        final DomainObject msg = context.getFormObjects().getRootDomainObject();

        String infoStrCreated = "Создан: ";
        String infoStrUpdated = "Изменен: ";

        Id createdID = msg.getReference("created_by");
        Id updatedID = msg.getReference("updated_by");
        Date createdDateInfo = msg.getCreatedDate();
        Date updatedDateInfo = msg.getModifiedDate();

        final SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");

        if (createdDateInfo != null) {
            infoStrCreated += f.format(createdDateInfo) + " - ";
        } else {
            infoStrCreated += " ";
        }

        if (updatedDateInfo != null) {
            infoStrUpdated += f.format(updatedDateInfo) + " - ";
        } else {
            infoStrUpdated += " ";
        }

        if (createdID == null || !this.crudService.exists(createdID))
            infoStrCreated +=  "Система";
        else {

            final DomainObject objMsg = this.crudService.find(createdID);

            String tStr = objMsg.getString("lastname");
            if (tStr!=null)
                infoStrCreated += tStr + " ";
            tStr = objMsg.getString("firstname");
            if (tStr!=null)
                infoStrCreated += tStr + " ";
        }

        if (updatedID == null || !this.crudService.exists(updatedID))
            infoStrUpdated +=  "Система";
        else {

            final DomainObject objMsg = this.crudService.find(updatedID);

            String tStr = objMsg.getString("lastname");
            if (tStr!=null)
                infoStrUpdated += tStr + " ";
            tStr = objMsg.getString("firstname");
            if (tStr!=null)
                infoStrUpdated += tStr + " ";
        }
        return infoStrCreated + "\n" + infoStrUpdated;
    }
}
