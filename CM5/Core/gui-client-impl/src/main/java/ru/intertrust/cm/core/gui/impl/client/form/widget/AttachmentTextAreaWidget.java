package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("attachment-text-area")
public class AttachmentTextAreaWidget extends AttachmentTextBoxWidget {
    @Override
    public Component createNew() {
        return new AttachmentTextAreaWidget();
    }

    protected Widget asEditableWidget(final WidgetState state) {
        return new TextArea();
    }
}
