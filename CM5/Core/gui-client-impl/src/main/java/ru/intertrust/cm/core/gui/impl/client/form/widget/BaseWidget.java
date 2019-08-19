package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RulesTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEvent;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionHelper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:40
 */
public abstract class BaseWidget extends BaseComponent implements IsWidget, CanBeValidated, WidgetBroadcastEventHandler {

  private EventBus applicationEventBus = Application.getInstance().getEventBus();

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

  /**
   * Обработчик общесистемных событий от виджетов.
   * Условие отработки:
   * Если виджет имеет подписку на ивенты от каких-то других виджетов (заполняется в хендлере  виджета)
   * Если событие каскадное и в списке публикаторов есть сам виджет значит событие прилетело в цикле, прерываем цепочку
   * initiatorHsahCode нужен чтобы определить что событие получил виджет который находится на той-же форме где это событие
   * было порождено т.к. в нашей системе формы даже после закрытия остаются висеть в DOM модели
   * При наличии правил вызывается метод их обработки реализующий специфичное поведение виджета настроеное конфигурацией правил.
   * По скольку методы получения значений у каждого виджета свои, реализация обработки правил так-же должна быть в классе
   * конкретного виджета.
   *
   * @param e
   */
  @Override
  public void onEventReceived(WidgetBroadcastEvent e) {
    if ((!initialData.getSubscription().isEmpty()
        && initialData.getSubscription().contains(e.getWidgetId())
        && (getContainer().hashCode() == e.getInitiatorHashCode())
    ) || (!initialData.getSubscription().isEmpty() && e.getBroadcast())) {
      if (e.getCascade() && e.getPublicatorsChain().contains(initialData.getWidgetId())) {
        return;
      }
      if (initialData.getRules() != null) {
        processRule(e.getBroadcast());
      }
    }
  }

  private void processRule(Boolean fromBroadcast) {
    try {
      // Первый приоритет
      if (initialData.getRules().getHideRulesTypeConfig() != null) {
        // Если правил сокрытия несколько то их суммарный результат должен быть true
        Boolean shouldByHidden = true;
        for (RuleTypeConfig rule : initialData.getRules().getHideRulesTypeConfig().getRuleTypeConfigs()) {
          shouldByHidden = shouldByHidden & ExpressionHelper.applyExpression(rule.getApplyExpression(), getContainer());
        }
        hide(!shouldByHidden);
      }
      if (initialData.getRules().getFilterRulesTypeConfig() != null && !fromBroadcast) {
        // Пока не ясно что делать с несколькими правилами фильтрации возникает противоречие.
        RuleTypeConfig filterRule = initialData.getRules().getFilterRulesTypeConfig().getRuleTypeConfigs().get(0);
        if (ExpressionHelper.applyExpression(filterRule.getApplyExpression(), getContainer())) {
          applyFilter(ExpressionHelper.getValue(filterRule.getValue(), getContainer()).toString());
        }
      }


      //TODO: Остальные типы проавил в следующих приоритетах разработки
    } catch (ExpressionException e) {
      Window.alert(e.getMessage());
    }
  }

  /**
   * Метод для правила Value. В случае динамического изменения значения виджета, например текста как результата
   * выбраного в другом виджете. Реализация зависима от виджета.
   *
   * @param value
   */
  public abstract void setValue(Object value);

  /**
   * Метод для правила Hide. Скрывает виджет
   * Согласно проектному решению, при скрытии данные виджета обнуляются.
   *
   * @param isHidden
   */
  public void hide(Boolean isHidden) {
    impl.setVisible(isHidden);
    if (!isHidden) {
      reset();
    }
  }

  /**
   * Метод для правила Access. Переводит виджет в состояние ReadOnly и обратно. Реализация зависима от виджета.
   *
   * @param isDisabled
   */
  public abstract void disable(Boolean isDisabled);

  /**
   * Метод для правила Reset. Сбрасывает значение виджета. Реализация зависима от виджета.
   */
  public abstract void reset();

  /**
   * Метод для правила Filter. Вызывает применение динамического фильтра у виджетов работающих на основе
   * коллекций данных. Реализация зависима от виджета.
   *
   * @param value
   */
  public abstract void applyFilter(String value);

  public abstract Object getValueTextRepresentation();


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

    if (getInitialData().isTranslateId()) {
      impl.getElement().setId(getDisplayConfig().getId());
    }
    applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    impl.addDomHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        applicationEventBus.fireEvent(new WidgetBroadcastEvent(getContainer(),initialData.getWidgetId(),
            getContainer().hashCode()
            , getContainer().getPlugin().getView().getActionToolBar().hashCode()));
      }
    }, ChangeEvent.getType());
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
  public abstract Object getValue();

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
   */
  public WidgetState getFullClientStateCopy() {
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

  protected LinkedHashMap<Id, String> getFilteredList(List<DomainObject> originalDo
      , LinkedHashMap<Id, String> originalDisplay
      , String value) {
    LinkedHashMap<Id, String> result = new LinkedHashMap<>();
    RulesTypeConfig rules = getInitialData().getRules();
    RuleTypeConfig filterRule = rules.getFilterRulesTypeConfig().getRuleTypeConfigs().get(0);
    result.put(null, "");
    for (DomainObject dObject : originalDo) {
      if (dObject.getValue(filterRule.getField()) instanceof StringValue
          && dObject.getString(filterRule.getField()).toLowerCase().trim().equals(value.toLowerCase().trim())) {
        result.put(dObject.getId(), originalDisplay.get(dObject.getId()));
      }
      if (dObject.getValue(filterRule.getField()) instanceof BooleanValue
          && dObject.getBoolean(filterRule.getField()).equals(Boolean.parseBoolean(value))) {
        result.put(dObject.getId(), originalDisplay.get(dObject.getId()));
      }
      if (dObject.getValue(filterRule.getField()) instanceof LongValue
          && dObject.getLong(filterRule.getField()).equals(Long.parseLong(value))) {
        result.put(dObject.getId(), originalDisplay.get(dObject.getId()));
      }
      if (dObject.getValue(filterRule.getField()) instanceof DateTimeValue
          && dObject.getDateTimeWithTimeZone(filterRule.getField()).toString().equals(value.trim())) {
        result.put(dObject.getId(), originalDisplay.get(dObject.getId()));
        //TODO: Необходимо проверить сравнение с присутствием таймзоны
      }
      if (dObject.getValue(filterRule.getField()) instanceof TimelessDateValue
          && dObject.getTimelessDate(filterRule.getField()).toString().equals(value.trim())) {
        result.put(dObject.getId(), originalDisplay.get(dObject.getId()));
      }
    }
    return result;
  }
}