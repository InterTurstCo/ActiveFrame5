package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentTextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("attachment-text-box")
public class AttachmentTextBoxWidget extends BaseWidget {
    @Override
    public Component createNew() {
        return new AttachmentTextBoxWidget();
    }

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    public void setCurrentState(WidgetState currentState) {
        setTrimmedText((HasText) impl, ((AttachmentTextState) currentState).getText());
    }

    @Override
    protected boolean isChanged() {
        String initValue = trim(((AttachmentTextState) getInitialData()).getText());
        final String currentValue = getTrimmedText((HasText) impl);
        return initValue == null ? currentValue != null : !initValue.equals(currentValue);
    }

    @Override
    protected WidgetState createNewState() {
        return new AttachmentTextState(
                ((AttachmentTextState) getInitialData()).getAttachmentId(),
                getTrimmedText((HasText) impl),
                isChanged());
    }

    @Override
    protected Widget asEditableWidget(final WidgetState state) {
        return new TextBox();
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        final Label label = new Label();
        label.getElement().addClassName("textBoxNonEditable");
        return label;
    }

    public Object getValue() {
        return ((HasText) impl).getText().trim();
    }

}
