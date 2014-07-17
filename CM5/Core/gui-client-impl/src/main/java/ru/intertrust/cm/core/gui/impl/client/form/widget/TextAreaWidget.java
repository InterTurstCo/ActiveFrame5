package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 10:59
 */
@ComponentName("text-area")
public class TextAreaWidget extends TextBoxWidget {
    @Override
    public TextAreaWidget createNew() {
        return new TextAreaWidget();
    }

    @Override
    protected TextState createNewState() {
        TextState data = new TextState();
        data.setText(getTrimmedText((HasText) impl));
        return data;
    }

    @Override
    protected boolean isChanged() {
        final String initText = ((TextState) getInitialData()).getText();
        final String initialValue = initText == null ? null : initText.trim();
        final String currentValue = getTrimmedText((HasText) impl);
        return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        TextBoxBase widget = ((TextState) state).isEncrypted() ? new PasswordTextBox() : new TextArea();
        widget.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                validate();
            }
        });
        return widget;
    }
}
