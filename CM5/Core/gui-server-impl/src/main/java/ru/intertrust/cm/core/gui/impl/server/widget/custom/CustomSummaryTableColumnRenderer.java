package ru.intertrust.cm.core.gui.impl.server.widget.custom;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.impl.server.widget.StringValueRenderer;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by andrey on 04.11.14.
 */
@ComponentName("summarytable.column.renderer")
public class CustomSummaryTableColumnRenderer implements StringValueRenderer, ComponentHandler{
    @Override
    public String render(DomainObject domainObject) {
        return "CustomSummaryTableColumnRenderer.render(DomainObject domainObject)";
    }

    @Override
    public String render(FormState formState) {
        return "CustomSummaryTableColumnRenderer.render(FormState formState)";
    }
}
