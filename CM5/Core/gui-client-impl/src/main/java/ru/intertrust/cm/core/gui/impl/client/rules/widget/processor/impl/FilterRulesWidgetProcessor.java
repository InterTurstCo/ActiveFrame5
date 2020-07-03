package ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl;

import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionHelper;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.IRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Процессор по обработке правил фильтрации значения(значений), применяемых к виджету<br>
 * <br>
 * Created by Myskin Sergey on 02.07.2020.
 */
public class FilterRulesWidgetProcessor implements IRulesWidgetProcessor {

    @Override
    public void process(BaseWidget widget) throws ExpressionException {
        // Пока не ясно что делать с несколькими правилами фильтрации возникает противоречие.
        final WidgetState initialData = widget.getInitialData();
        RuleTypeConfig filterRule = initialData.getRules().getFilterRulesTypeConfig().getRuleTypeConfigs().get(0);
        if (ExpressionHelper.applyExpression(filterRule.getApplyExpression(), widget.getContainer())) {
            widget.applyFilter(ExpressionHelper.getValue(filterRule.getValue(), widget.getContainer()).toString());
        }
    }

    @Override
    public int getOrder() {
        return 40;
    }

}
