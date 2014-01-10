package ru.intertrust.cm.core.gui.api.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.13
 *         Time: 14:58
 */
public abstract class LinkEditingWidgetHandler extends WidgetHandler {
    @Autowired
    protected ConfigurationService configurationService;

    @Override
    public Value getValue(WidgetState state) {
        ArrayList<Id> ids = ((LinkEditingWidgetState) state).getIds();
        return ids == null || ids.isEmpty() ? null : new ReferenceValue(ids.get(0));
    }

    protected String getLinkedObjectType(WidgetContext context, FieldPath fieldPath) {
        if (fieldPath.isField()) {
            // such situation happens when link-editing widget is assigned to a field which is actually a reference
            String parentType = context.getFormObjects().getParentNode(fieldPath).getType();
            return ((ReferenceFieldConfig) configurationService.getFieldConfig(parentType, fieldPath.getFieldName())).getType();
        }

        if (fieldPath.isOneToManyReference()) {
            return fieldPath.getReferenceType();
        } else { // many-to-many
            return ((ReferenceFieldConfig) configurationService.getFieldConfig(
                    fieldPath.getReferenceType(), fieldPath.getLinkToChildrenName())).getType();
        }
    }

    protected String[] getLinkedObjectTypes(WidgetContext context, FieldPath[] fieldPaths) {
        String[] result = new String[fieldPaths.length];
        for (int i = 0; i < fieldPaths.length; ++i) {
            result[i] = getLinkedObjectType(context, fieldPaths[i]);
        }
        return result;
    }

    public void saveNewObjects(WidgetContext context, WidgetState state) {
    }
}
