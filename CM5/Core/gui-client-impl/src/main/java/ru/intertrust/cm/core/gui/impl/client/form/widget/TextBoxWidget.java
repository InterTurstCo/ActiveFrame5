package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.validation.*;

import java.util.Collection;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("text-box")
public class TextBoxWidget extends BaseWidget {

    private String confirmation;
    private String confirmationFor;

    @Override
    public Component createNew() {
        return new TextBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        setTrimmedText((HasText) impl, ((TextState) currentState).getText());
    }

    @Override
    protected boolean isChanged() {
        final String initData = ((TextState) getInitialData()).getText();
        final String initValue = initData == null ? null : initData.trim();
        final String currentValue = getTrimmedText((HasText) impl);
        return initValue == null ? currentValue != null : !initValue.equals(currentValue);
    }

    @Override
    protected WidgetState createNewState() {
        TextState data = new TextState();
        data.setText(getTrimmedText((HasText) impl));
        return data;
    }

    @Override
    protected Widget asEditableWidget(final WidgetState state) {

        TextBox textBox = state instanceof TextState && ((TextState) state).isEncrypted() ? new PasswordTextBox() : new TextBox();

        textBox.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {

                if(((TextState) state).getPasswordConfirmationId() != null){

                    confirmationFor = ((TextState) state).getPasswordConfirmationId();
                    confirmation = ((TextState) state).getPasswordWidgetId();

                    clearErrors();
                    if (!validateConfirmation()) {
                        showErrors(new ValidationResult());
                    }
                    if(((TextState) state).getPasswordConfirmationId() == null){
                        validate();
                    }
                }

                if(((TextState) state).getPasswordConfirmationId() == null){
                    validate();
                }

            }
        });
        return textBox;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = super.validate();

        if (confirmationFor != null) {
            if (!validateConfirmation()) {
                validationResult.addError("Поля пароль/подтверждение не совпадают!");
                showErrors(validationResult);
            }
        }
        return validationResult;
    }

    private boolean validateConfirmation() {
        boolean result;
        result = getValueFromWidgetById(confirmationFor).equals(getValueFromWidgetById(confirmation));


        return result;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        if (state instanceof TextState && ((TextState) state).isEncrypted()) {
            final PasswordTextBox passwordTextBox = new PasswordTextBox();
            passwordTextBox.setEnabled(false);
            return passwordTextBox;
        }
        final Label label = new Label();
        label.getElement().addClassName("textBoxNonEditable");
        return label;
    }

    public String getValueFromWidgetById(String id) {
        TextBoxWidget baseWidget = this.getContainer().getWidget(id);
        String result = (String) baseWidget.getValue();
        return result;
    }

    public Object getValue() {
        return ((HasText) impl).getText().trim();
    }

}
