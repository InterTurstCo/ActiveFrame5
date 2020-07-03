package ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl;

import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionHelper;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.IRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Процессор по обработке правил доступа, применяемых к виджету<br>
 * <br>
 * Created by Myskin Sergey on 02.07.2020.
 */
public class AccessRulesWidgetProcessor implements IRulesWidgetProcessor {

    @Override
    public void process(BaseWidget widget) throws ExpressionException {
        // Если правил доступа несколько то достаточно одного негативного результата, чтобы суммарный результат был так же негавтиным
        boolean hasAccess = true;
        final WidgetState initialData = widget.getInitialData();
        for (RuleTypeConfig rule : initialData.getRules().getAccessRulesTypeConfig().getRuleTypeConfigs()) {
            hasAccess = ExpressionHelper.applyExpression(rule.getApplyExpression(), widget.getContainer());
            if (!hasAccess) {
                break;
            }
        }
        widget.disable(!hasAccess);
    }

    @Override
    public int getOrder() {
        return 20;
    }

}
