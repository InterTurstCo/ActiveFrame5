package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.impl.client.util.StringUtil;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.validation.CanBeValidated;
import ru.intertrust.cm.core.gui.model.validation.SimpleValidator;
import ru.intertrust.cm.core.gui.model.validation.ValidationMessage;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.model.validation.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:40
 */
public abstract class BaseWidget extends BaseComponent implements IsWidget, CanBeValidated {
    protected WidgetState initialData;
    protected WidgetDisplayConfig displayConfig;
    protected boolean isEditable = true;
    protected EventBus eventBus;
    protected Widget impl;

    private Map<String, String> messages;
    private List<String> constraints = new ArrayList<String>();

    public <T extends WidgetState> T getInitialData() {
        return (T) initialData;
    }

    public void setInitialData(WidgetState initialData) {
        this.initialData = initialData;
    }

    public WidgetDisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    public void setDisplayConfig(WidgetDisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected List<String> getMessages(ValidationResult validationResult) {
        List<String> messages = new ArrayList<String>();
        for (ValidationMessage msg : validationResult.getMessages()) {
            messages.add(getMessageText(msg.getMessage()));
        }
        return messages;
    }

    protected String getMessageText(String messageKey) {
        Map<String, Object> props = getInitialData().getWidgetProperties();
        props.put("value", getValue()); // TODO: [validation] use constants
        if (messages.get(messageKey) != null) {
            return PlaceholderResolver.substitute(messages.get(messageKey), props);
        } else {
            return messageKey;//let's return at list messageKey if the message is not found
        }
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    @Override
    public Widget asWidget() {
        return impl;
    }

    public void setState(WidgetState state) {
        if (impl == null) {
            impl = isEditable ? asEditableWidget(state) : asNonEditableWidget(state);
            applySizeTo(impl);
        }
        setCurrentState(state);
        this.initialData = state;
        constraints = state.getConstraints();
    }

    public abstract void setCurrentState(WidgetState currentState);

    /**
     * Значение, введенное пользователем. Метод должен быть переопределен для виджетов,
     * выполняющих клиентскую валидацию, и должен возвращать данные, введенные пользователем, в исходном виде
     * (т.е. до проверок и преобразований).
     * @return данные введенные пользователем.
     */
    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public List<Validator> getValidators() {
        List<Validator> validators = new ArrayList<Validator>();
        for (String constraint : getConstraints()) { // TODO: [validation] support other types of validators
            validators.add(new SimpleValidator(constraint));
        }
        return validators;
    }

    public ValidationResult validate() {
        clearErrors();
        Collection<Validator> validators = getValidators();
        ValidationResult validationResult = new ValidationResult();
        for (Validator validator : validators) {
            validationResult.append(validator.validate(this, null));
        }
        return  validationResult;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    // todo: setNonEditableState, getNonEditableState

    /**
     * Возвращает текущее состояние виджета. Если виджет в режиме "только чтение", возвращает null
     * @return текущее состояние виджета или null, если виджет в режиме "только чтение"
     */
    public abstract WidgetState getCurrentState();

    protected abstract Widget asEditableWidget(WidgetState state);

    protected abstract Widget asNonEditableWidget(WidgetState state);

    protected void applySizeTo(Widget widget) {
        String width = displayConfig.getWidth();
        String height = displayConfig.getHeight();
        if (width != null && !width.isEmpty()) {
            widget.setWidth(width);
        }
        if (height != null && !height.isEmpty()) {
            widget.setHeight(height);
        }
    }

    protected static String getTrimmedText(HasText widget) {
        String text = widget.getText();
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }
        return trimmedText;
    }

    protected static void setTrimmedText(HasText widget, String text) {
        widget.setText(text == null ? "" : text.trim());
    }

    @Override
    public void showErrors(ValidationResult errors) {
        String errorString = StringUtil.join(getMessages(errors), "\n");
        if (impl.getTitle() != null) {
            errorString = impl.getTitle() + errorString;
        }
        impl.setTitle(errorString);
        impl.addStyleName("validation-error");
    }

    @Override
    public void clearErrors() {
        impl.setTitle(null);
        impl.removeStyleName("validation-error");
    }
}
