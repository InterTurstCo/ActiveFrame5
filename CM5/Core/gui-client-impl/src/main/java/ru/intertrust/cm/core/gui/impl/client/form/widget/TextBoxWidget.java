package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

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
                if (state instanceof TextState) {
                    TextState texState = (TextState) state;
                    confirmation = ((TextState) state).getPrimaryWidgetId();
                    confirmationFor = ((TextState) state).getConfirmationWidgetId();
                    validateTextState(texState);
                } else {
                    validate();
                }
            }
        });
        return textBox;

    }

    private void validateTextState(TextState textState) {
        if (textState.getConfirmationWidgetId() == null && textState.getPrimaryWidgetId() == null) {
            validate();
        }
        if (textState.getConfirmationWidgetId() != null & textState.getPrimaryWidgetId() != null) {
            clearErrors();
            if (!validateConfirmation()) {
                showErrors(new ValidationResult());
            }
            else{
                clearErrors();
            }
        }
        else{
            validate();
        }
    }


    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = super.validate();
        if(confirmation != null || confirmationFor != null){
            clearErrors();
            if (!validateConfirmation()) {
                validationResult.addError("Поля пароль/подтверждение не совпадают!");
                showErrors(new ValidationResult());
            }
        }
        return validationResult;
    }

    private boolean validateConfirmation() {
        boolean result = false;
        if (confirmation != null && confirmationFor != null) {
            result = getValueFromWidgetById(confirmation).equals(getValueFromWidgetById(confirmationFor));
        }

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
        if (this.getContainer().getWidget(id) == null) {
            throw new ConfigurationException("widgetId is null");
        }
        String result = (String) baseWidget.getValue();
        return result;
    }

    public Object getValue() {
        return ((HasText) impl).getText().trim();
    }

}
