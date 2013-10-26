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
        FieldPath.Element lastElement = fieldPath.getLastElement();
        if (lastElement instanceof FieldPath.Field) {
            String parentType = context.getFormObjects().getNode(fieldPath.getParentPath()).getType();
            return ((ReferenceFieldConfig) configurationService.getFieldConfig(parentType, lastElement.getName())).getType();
        }

        String referenceType = ((FieldPath.BackReference) lastElement).getReferenceType();
        if (lastElement instanceof FieldPath.OneToManyBackReference) {
            return referenceType;
        } else { // many-to-many
            String linkToChildrenName = ((FieldPath.ManyToManyReference) lastElement).getLinkToChildrenName();
            return ((ReferenceFieldConfig) configurationService.getFieldConfig(referenceType, linkToChildrenName)).getType();
        }
    }

    public void saveNewObjects(WidgetContext context, WidgetState state) {
    }
}
