package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Created by andrey on 04.11.14.
 */
public interface StringValueRenderer {
    String render(DomainObject domainObject);
    String render(FormState formState);
}
