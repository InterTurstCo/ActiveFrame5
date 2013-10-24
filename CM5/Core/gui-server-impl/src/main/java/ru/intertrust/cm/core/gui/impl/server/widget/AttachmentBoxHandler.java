package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.config.model.gui.form.widget.AttachmentBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.MultiObjectWidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 10:25
 */
@ComponentName("attachment-box")
public class AttachmentBoxHandler  extends MultiObjectWidgetHandler {

    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        LinkedHashMap<String, Id> fileNameMaps = new LinkedHashMap<String, Id>();

        ObjectsNode node = context.getFormObjects().getObjects(fieldPath);
        Iterator<DomainObject> iterator = node.iterator();
        while (iterator.hasNext()) {
            DomainObject temp = iterator.next();
           for (String field : temp.getFields()) {
                if ("Name".equalsIgnoreCase(field)){
                    fileNameMaps.put((String) temp.getValue(field).get(), temp.getId());
                }
            }
        }
        fileNameMaps.put("test", new RdbmsId());
        fileNameMaps.put("test2", new RdbmsId());
        AttachmentBoxState result = new AttachmentBoxState();
        result.setListValues(fileNameMaps);
        return result;
    }
}
