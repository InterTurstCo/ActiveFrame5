package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEvent;
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
        setTrimmedText((HasText) impl, null);
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
        setTrimmedText((HasText) impl, ((TextState) currentState).getText());
    }

    @Override
    protected boolean isChanged() {
        String initValue = trim(((TextState) getInitialData()).getText());
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
        if (state instanceof TextState) {
            confirmation = ((TextState) state).getPrimaryWidgetId();
            confirmationFor = ((TextState) state).getConfirmationWidgetId();
        }
        textBox.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if (state instanceof TextState) {
                    validateTextState(confirmation, confirmationFor);
                } else {
                    validate();
                }
            }
        });
        textBox.ensureDebugId(BaseWidget.TEXT_BOX+getDisplayConfig().getParentName()+"-"+getDisplayConfig().getId());
        return textBox;

    }

    private void validateTextState(String confirmation, String confirmationFor) {
        if (confirmation == null && confirmationFor == null) {
            validate();
        }
        if (confirmationFor != null & confirmation != null) {
            clearErrors();
            if (!validateConfirmation()) {
                showPasswordErrors(confirmation, confirmationFor);
            }
            else{
                clearPasswordErrors(confirmation, confirmationFor);
            }
        }
        else{
            validate();
        }
    }

    private  void showPasswordErrors(String password, String confirmation){
        getWidgetFrom(password).showErrors(new ValidationResult());
        getWidgetFrom(confirmation).showErrors(new ValidationResult());
    }
    private  void clearPasswordErrors(String password, String confirmation){
        getWidgetFrom(password).clearErrors();
        getWidgetFrom(confirmation).clearErrors();
    }

    private TextBoxWidget getWidgetFrom(String id){
        return this.getContainer().getWidget(id);
    }

    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = super.validate();

        if(confirmation != null || confirmationFor != null){
            //clearErrors();
            if (!validateConfirmation()) {
                validationResult.addError("Поля пароль/подтверждение не совпадают!");
                showErrors(validationResult);
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
