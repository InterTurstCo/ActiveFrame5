package ru.intertrust.cm.core.gui.impl.server.widget.report;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.TextBoxHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;

@ComponentName("report.template.name.handler")
public class ReportTemplateNameHandler extends TextBoxHandler {

    @Override
    public TextState getInitialState(WidgetContext context) {
        final TextState textState = super.getInitialState(context);
        FormObjects formObjects = context.getFormObjects();
        DomainObject rootDomainObject = formObjects.getRootDomainObject();
        boolean isNew = rootDomainObject.isNew();
        textState.setForceReadOnly(!isNew);
        return textState;
    }

}
