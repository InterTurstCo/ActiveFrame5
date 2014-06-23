package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 *         Date: 18.06.2014
 *         Time: 16:03
 */
public class DomainObjectLinkContext {
    private FormState formState;
    private DomainObject parentObject;
    private DomainObject linkedObject;
    private WidgetContext widgetContext;
    private FieldPath fieldPath;

    public DomainObjectLinkContext() {
    }

    public DomainObjectLinkContext(FormState formState, DomainObject parentObject, DomainObject linkedObject, WidgetContext widgetContext, FieldPath fieldPath) {
        this.formState = formState;
        this.parentObject = parentObject;
        this.linkedObject = linkedObject;
        this.widgetContext = widgetContext;
        this.fieldPath = fieldPath;
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }

    public DomainObject getParentObject() {
        return parentObject;
    }

    public void setParentObject(DomainObject parentObject) {
        this.parentObject = parentObject;
    }

    public DomainObject getLinkedObject() {
        return linkedObject;
    }

    public void setLinkedObject(DomainObject linkedObject) {
        this.linkedObject = linkedObject;
    }

    public WidgetContext getWidgetContext() {
        return widgetContext;
    }

    public void setWidgetContext(WidgetContext widgetContext) {
        this.widgetContext = widgetContext;
    }

    public FieldPath getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(FieldPath fieldPath) {
        this.fieldPath = fieldPath;
    }
}
