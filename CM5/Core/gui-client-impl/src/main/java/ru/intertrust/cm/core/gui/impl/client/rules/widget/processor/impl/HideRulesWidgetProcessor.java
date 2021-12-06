package ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl;

import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionHelper;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.IRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Процессор по обработке правил скрытия/отображения, применяемых к виджету<br>
 * <br>
 * Created by Myskin Sergey on 30.06.2020.
 */
public class HideRulesWidgetProcessor implements IRulesWidgetProcessor {

    @Override
    public void process(BaseWidget widget) throws ExpressionException {
        // Если правил сокрытия несколько то их суммарный результат должен быть true
        Boolean shouldByHidden = true;
        final WidgetState initialData = widget.getInitialData();
        for (RuleTypeConfig rule : initialData.getRules().getHideRulesTypeConfig().getRuleTypeConfigs()) {
            shouldByHidden = shouldByHidden && ExpressionHelper.applyExpression(rule.getApplyExpression(), widget.getContainer());
        }
        widget.hide(!shouldByHidden);
    }

    @Override
    public int getOrder() {
        return 10;
    }

}
