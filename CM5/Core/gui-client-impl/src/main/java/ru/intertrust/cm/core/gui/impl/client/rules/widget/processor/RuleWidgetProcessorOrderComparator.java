package ru.intertrust.cm.core.gui.impl.client.rules.widget.processor;

import java.util.Comparator;

/**
 * Сортировщик порядка применения правил, на основе значения приоритета сортировки<br>
 * <br>
 * Created by Myskin Sergey on 02.07.2020.
 */
public class RuleWidgetProcessorOrderComparator implements Comparator<IRulesWidgetProcessor> {

    @Override
    public int compare(IRulesWidgetProcessor rp1, IRulesWidgetProcessor rp2) {
        final int order1 = rp1.getOrder();
        final int order2 = rp2.getOrder();

        return Integer.compare(order1, order2);
    }

}
