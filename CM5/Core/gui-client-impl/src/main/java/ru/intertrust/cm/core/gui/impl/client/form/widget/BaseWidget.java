package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.validation.CanBeValidated;
import ru.intertrust.cm.core.gui.model.validation.DecimalRangeValidator;
import ru.intertrust.cm.core.gui.model.validation.IntRangeValidator;
import ru.intertrust.cm.core.gui.model.validation.LengthValidator;
import ru.intertrust.cm.core.gui.model.validation.ScaleAndPrecisionValidator;
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

    protected static final String TEXT_BOX = "text-box_";
    protected static final String TEXT_AREA = "text-area_";
    protected static final String RADIO_BUTTON = "radio_button_";
    protected static final String SG_BOX = "suggest_box_";
    protected static final String SG_BOX_ARROW = "suggest_box_arrow_";
    protected static final String SG_BOX_CLR = "suggest_box_clear_";

    protected WidgetState initialData;
    protected WidgetDisplayConfig displayConfig;
    protected boolean isEditable = true;
    protected EventBus eventBus;
    protected Widget impl;
    protected WidgetsContainer container;

    private Map<String, String> messages;

    public final boolean isDirty() {
        if (isEditable()) {
            return isChanged();
        } else {
            return false;
        }
    }

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
        props.put(Constraint.VAlUE, getValue());
        String value = messages.get(messageKey);
        if (value == null) {
            value = LocalizationKeys.validationMessages.get(messageKey);
        }
        if (value != null) {
            return PlaceholderResolver.substitute(value, props);
        } else {
            return messageKey;//let's return at least messageKey if the message is not found
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
    }


    public abstract void setCurrentState(WidgetState currentState);

    /**
     * Значение, введенное пользователем. Метод должен быть переопределен для виджетов,
     * выполняющих клиентскую валидацию, и должен возвращать данные, введенные пользователем, в исходном виде
     * (т.е. до проверок и преобразований).
     *
     * @return данные введенные пользователем.
     */
    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public List<Validator> getValidators() {
        List<Validator> validators = new ArrayList<Validator>();
        for (Constraint constraint : getInitialData().getConstraints()) {
            switch (constraint.getType()) {
                case SIMPLE:
                    validators.add(new SimpleValidator(constraint));
                    break;
                case LENGTH:
                    validators.add(new LengthValidator(constraint));
                    break;
                case INT_RANGE:
                    validators.add(new IntRangeValidator(constraint));
                    break;
                case DECIMAL_RANGE:
                    validators.add(new DecimalRangeValidator(constraint));
                    break;
                case SCALE_PRECISION:
                    validators.add(new ScaleAndPrecisionValidator(constraint));
                    break;
            }
        }
        return validators;
    }

    public ValidationResult validate() {
        clearErrors();
        ValidationResult validationResult = new ValidationResult();
        if (!isEditable()) {
            return validationResult;
        }
        Collection<Validator> validators = getValidators();
        for (Validator validator : validators) {
            validationResult.append(validator.validate(this, null));
        }
        return validationResult;
    }

    // todo: setNonEditableState, getNonEditableState

    /**
     * Возвращает текущее состояние виджета. Если виджет в режиме "только чтение", возвращает null
     *
     * @return текущее состояние виджета или null, если виджет в режиме "только чтение"
     */
    public final WidgetState getCurrentState() {
        WidgetState state = createNewState();
        state.setConstraints(getInitialData().getConstraints());
        return state;
    }
    /**
     * Получения промежуточного состояния виджета, когда он еще не сохранен, но может быть отредактирован.
     * Используется для LinkedDomainObjectsTableWidget.
     * */
    public WidgetState getFullClientStateCopy(){
        return getCurrentState();
    }

    protected abstract boolean isChanged();

    protected abstract WidgetState createNewState();

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
        return trim(widget.getText());
    }

    protected static String trim(String text) {
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

    /**
     * Устанавливает фокус на лежащий в основе GWT-виджет, если это возможно<br>
     * (виджет должен имплементировать интерфейс {@link com.google.gwt.user.client.ui.FocusWidget FocusWidget})
     *
     * @return true - фокус был установлен<br>
     * false - не был
     */
    public boolean focus() {
        final boolean isFocused = GuiUtil.focusWidget(impl);
        return isFocused;
    }

    public WidgetsContainer getContainer() {
        return container;
    }

    public void setContainer(WidgetsContainer container) {
        this.container = container;
    }
}