package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("text-box")
public class TextBoxWidget extends BaseWidget {
    @Override
    public Component createNew() {
        return new TextBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        setTrimmedText((HasText) impl, ((TextState) currentState).getText());
    }

    @Override
    protected WidgetState createNewState() {
        TextState data = new TextState();
        data.setText(getTrimmedText((HasText) impl));
        return data;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        TextBox textBox = state instanceof TextState && ((TextState) state).isEncrypted() ? new PasswordTextBox() : new TextBox();
        textBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                validate();
            }
        });
        return textBox;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        if (state instanceof TextState && ((TextState) state).isEncrypted()) {
            final PasswordTextBox passwordTextBox = new PasswordTextBox();
            passwordTextBox.setEnabled(false);
            return passwordTextBox;
        }
        final Label label = new Label();
        label.setStyleName("");
        return label;
    }

    public Object getValue() {
        return ((HasText) impl).getText().trim();
    }

}
