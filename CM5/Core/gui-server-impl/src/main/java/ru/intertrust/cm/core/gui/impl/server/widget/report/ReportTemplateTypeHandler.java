package ru.intertrust.cm.core.gui.impl.server.widget.report;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.EnumBoxHandler;
import ru.intertrust.cm.core.gui.impl.server.widget.TextBoxHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.EnumBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;

@ComponentName("report.template.type.handler")
public class ReportTemplateTypeHandler extends EnumBoxHandler {

    @Override
    public EnumBoxState getInitialState(WidgetContext context) {
        // return super.getInitialState(context);
        final EnumBoxState enumBoxState = super.getInitialState(context);
        FormObjects formObjects = context.getFormObjects();
        DomainObject rootDomainObject = formObjects.getRootDomainObject();
        boolean isNew = rootDomainObject.isNew();
        if (isNew && (enumBoxState.getSelectedText() == null || enumBoxState.getSelectedText().isEmpty())) {
            enumBoxState.setSelectedText("Печатная форма");
        }
        return enumBoxState;
    }

}
