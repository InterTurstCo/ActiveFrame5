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
public abstract class MultiObjectWidgetHandler extends WidgetHandler {
    @Autowired
    protected ConfigurationService configurationService;

    protected String getLinkedObjectType(WidgetContext context, FieldPath fieldPath) {
        //todo after country_city^country.city is transformed to an Object, but not to a field lots of if-else will go
        String backlink = fieldPath.getLastElement().getName();
        String[] backlinkTypeAndField = backlink.split("\\^");
        boolean oneToMany = backlinkTypeAndField.length == 2;
        String linkType;
        if (oneToMany) {
            linkType = backlinkTypeAndField[0];
        } else { // many-to-many
            String parentType = context.getFormObjects().getObjects(fieldPath.getParentPath()).getType();
            linkType = ((ReferenceFieldConfig) configurationService.getFieldConfig(parentType, backlink)).getType();
        }
        return linkType;
    }

    public void saveNewObjects(WidgetContext context, WidgetState state) {
    }
}
