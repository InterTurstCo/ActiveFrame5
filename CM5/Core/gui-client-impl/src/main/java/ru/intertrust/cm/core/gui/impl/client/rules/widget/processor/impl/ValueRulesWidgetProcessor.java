package ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl;

import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionHelper;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.IRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Процессор по обработке правил установки значения, применяемых к виджету<br>
 * <br>
 * Created by Myskin Sergey on 02.07.2020.
 */
public class ValueRulesWidgetProcessor implements IRulesWidgetProcessor {

    @Override
    public void process(BaseWidget widget) throws ExpressionException {
        // TODO : Подумать как это решить в случае противоречия (сработало сразу несколько условий), возможно нужно добавить какой-то флаг приоритета правила установки значения
        // в случае сработки нескольких условий, будет установлено значение последнего
        Object value = null;
        final WidgetState initialData = widget.getInitialData();
        for (RuleTypeConfig rule : initialData.getRules().getValueRulesTypeConfig().getRuleTypeConfigs()) {

            final boolean applyExpression = ExpressionHelper.applyExpression(rule.getApplyExpression(), widget.getContainer());
            if (applyExpression) {
                value = ExpressionHelper.getValue(rule.getValue(), widget.getContainer());
            }
        }
        if (value != null) {
            widget.setValue(value);
        }
    }

    @Override
    public int getOrder() {
        return 30;
    }

}
