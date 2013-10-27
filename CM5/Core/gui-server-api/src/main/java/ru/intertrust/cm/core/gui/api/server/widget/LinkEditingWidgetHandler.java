package ru.intertrust.cm.core.gui.api.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.13
 *         Time: 14:58
 */
public abstract class LinkEditingWidgetHandler extends WidgetHandler {
    @Autowired
    protected ConfigurationService configurationService;

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

    public void saveNewObjects(WidgetContext context, WidgetState state) {
    }
}
