package ru.intertrust.cm.core.gui.impl.client.rules;

import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.ComboBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.ListBoxWidget;

/**
 * Класс предназначен для работы с выражениями написаными при конфигурации бизнес-правил
 * Основной единицей с которой работает класс является понятие виджета
 * Виджет описывается как widgetName.value где WidgetName это идентификатор (Id) виджета который задан в файле
 * конфигурации и транслирован в JS при помощи атрибута translate-id="true"
 * Парсинг выражения заключается в извлечении идентификаторов виджетов, получения к ним доступа в контейнере
 * и последующем получении значения виджета методом getValue. Затем производится сравнение либо другая необходимая
 * операция.
 */
public class ExpressionHelper {
  private static final String VAL = ".value";
  private static final String AND = " and ";
  private static final String OR = " or ";
  private static final String EQ = "==";
  private static final String NOTEQ = "!=";
  private static final String GREAT = ">";
  private static final String LESS = "<";

  public static Boolean applyExpression(String expression, WidgetsContainer container) throws ExpressionException {
    // Если apply-expression не задано то правило применяется в любом случае
    if (expression != null && expression.trim() != "") {
      validate(expression.toLowerCase());

      if (expression.toLowerCase().contains(AND)) {
        String[] expressions = splitExpressionByLogicalOperation(expression.toLowerCase(), AND);
        Boolean firstExpression = calculate(expressions[0], container);
        Boolean secondExpression = calculate(expressions[1], container);
        return firstExpression & secondExpression;
      } else if (expression.toLowerCase().contains(OR)) {
        String[] expressions = splitExpressionByLogicalOperation(expression.toLowerCase(), OR);
        Boolean firstExpression = calculate(expressions[0], container);
        Boolean secondExpression = calculate(expressions[1], container);
        return firstExpression || secondExpression;
      } else {
        return calculate(expression, container);
      }
    } else return true;

  }

  public static Object getValue(String expression, WidgetsContainer container) throws ExpressionException {
    String operand = expression.trim();
    if (operand.toLowerCase().contains(VAL)) {
      try {
        String widgetId = operand.substring(0, operand.toLowerCase().indexOf(VAL));
        Object value = getValueByWidgetId(widgetId, container);
        return value;
      } catch (ExpressionException e) {
        throw new ExpressionException(e.getMessage());
      }
    }
    // в выражении может быть задано константное значение, в таком случае просто возвращаем его
    return operand;
  }

  private static Object getValueByWidgetId(String wId, WidgetsContainer container) throws ExpressionException {
    for (BaseWidget w : container.getWidgets()) {
      if (w.getInitialData() != null
          && w.getInitialData().getWidgetId() != null
          && w.getInitialData().getWidgetId().toLowerCase().equals(wId.toLowerCase())) {
          return w.getValueTextRepresentation();
      }
    }
    throw new ExpressionException("Не найден виджет с идентификатором " + wId);
  }

  private static void validate(String expression) throws ExpressionException {
    if (expression.contains(AND) && expression.contains(OR)) {
      throw new ExpressionException("Не допускается одновременное использование AND и OR в выражении. Ошибка: [" + expression + "]");
    }
    if (expression.contains(AND) && expression.indexOf(AND) != expression.lastIndexOf(AND)) {
      throw new ExpressionException("Не допускается повторное использование AND в выражении. Ошибка: [" + expression + "]");
    }
    if (expression.contains(OR) && expression.indexOf(OR) != expression.lastIndexOf(OR)) {
      throw new ExpressionException("Не допускается повторное использование OR в выражении. Ошибка: [" + expression + "]");
    }
  }

  private static Boolean calculate(String expression, WidgetsContainer container) throws ExpressionException {
    Object leftOperandValue;
    Object rightOperandValue;
    if (expression.trim().equals("")) {
      throw new ExpressionException("Пустое выражение не допустимо");
    }
    String[] operands = splitExpressionIntoOperands(expression.trim());
    leftOperandValue = getValue(operands[0].trim(), container);
    rightOperandValue = getValue(operands[1].trim(), container);
    if (leftOperandValue instanceof Boolean) {
      if (!rightOperandValue.toString().toLowerCase().equals("true")
          && !rightOperandValue.toString().toLowerCase().equals("false")) {
        throw new ExpressionException("Не верно логическое значение. Допускается (true,false) Ошибка: [" + rightOperandValue + "]");
      }
      return (expression.contains(EQ)) ? leftOperandValue.equals(Boolean.parseBoolean(rightOperandValue.toString())) :
          !leftOperandValue.equals(Boolean.parseBoolean(rightOperandValue.toString()));
    }

    if (leftOperandValue!=null && leftOperandValue instanceof String) {
      if (rightOperandValue.toString().indexOf("'") == 0
          && rightOperandValue.toString().lastIndexOf("'") == rightOperandValue.toString().length() - 1) {
        if (expression.contains(EQ)) {
          return ((String) leftOperandValue).toLowerCase().equals(rightOperandValue.toString().substring(1, rightOperandValue.toString().length() - 1));
        } else if (expression.contains(NOTEQ)) {
          return !((String) leftOperandValue).toLowerCase().equals(rightOperandValue.toString().substring(1, rightOperandValue.toString().length() - 1));
        } else {
          throw new ExpressionException("Операция не применима к строкам. Допускается ==,!=");
        }
      }  else {
        throw new ExpressionException("Строковое значение должно быть заключено в кавычки '' ");
      }
    }
    return false;
  }

  private static String[] splitExpressionIntoOperands(String expression) throws ExpressionException {
    if (expression.contains(EQ)) {
      return expression.trim().split(EQ);
    } else if (expression.contains(NOTEQ)) {
      return expression.trim().split(NOTEQ);
    } else if (expression.contains(GREAT)) {
      return expression.trim().split(GREAT);
    } else if (expression.contains(LESS)) {
      return expression.trim().split(LESS);
    } else {
      throw new ExpressionException("Не верно задано выражение сравнения. Допускается (==,!=,>,<) Ошибка: [" + expression + "]");
    }
  }

  private static String[] splitExpressionByLogicalOperation(String expression, String logicalOperation) {
    return expression.split(logicalOperation);
  }

  /**
   * Метод позволяющий получить значение поля доменного обьекта
   * @param container
   * @return
   */
  private static String getFormObjectFieldValue(WidgetsContainer container){
    return ((FormPlugin)container.getPlugin()).getFormState().getObjects().getRootDomainObject().getString("");
  }
}
