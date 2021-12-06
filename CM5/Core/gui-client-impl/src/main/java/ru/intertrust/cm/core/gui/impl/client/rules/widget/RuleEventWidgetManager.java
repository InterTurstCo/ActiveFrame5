package ru.intertrust.cm.core.gui.impl.client.rules.widget;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.config.gui.form.widget.RulesTypeConfig;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.IRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.RuleWidgetProcessorOrderComparator;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl.AccessRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl.HideRulesWidgetProcessor;
import ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.impl.ValueRulesWidgetProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Обработчика правил по событиям для виджета.
 * <p>
 * Created by Myskin Sergey on 30.06.2020.
 */
public class RuleEventWidgetManager<T extends BaseWidget> {

    private T widget;

    public RuleEventWidgetManager(T widget) {
        this.widget = widget;
    }

    /**
     * Обрабатывает полученное событие для виджета.
     *
     * @param e объект события.
     */
    public void processReceivedEvent(WidgetBroadcastEvent e) {
        if (
                (!widget.getInitialData().getSubscription().isEmpty()
                        && widget.getInitialData().getSubscription().contains(e.getWidgetId())
                        //&& (getContainer().hashCode() == e.getInitiatorHashCode())
                ) || (!widget.getInitialData().getSubscription().isEmpty() && e.getBroadcast())
        ) {
            if (e.getCascade() && e.getPublicatorsChain().contains(widget.getInitialData().getWidgetId())) {
                return;
            }
            if (widget.getInitialData().getRules() != null) {
                processRules(e.getBroadcast());
            }
        }
    }

    /**
     * Обрабатывает правила, если таковые были найдены.<br>
     * Для каждого установленного правила создается процессор и в нужном порядке вызываются обработки.
     *
     * @param fromBroadcast
     */
    private void processRules(Boolean fromBroadcast) {
        final List<IRulesWidgetProcessor> allRequiredRuleProcessors = createAllRequiredRuleProcessors(fromBroadcast);
        Collections.sort(allRequiredRuleProcessors, new RuleWidgetProcessorOrderComparator());

        final Iterator<IRulesWidgetProcessor> iterator = allRequiredRuleProcessors.iterator();
        try {
            while (iterator.hasNext()) {
                final IRulesWidgetProcessor rulesProcessor = iterator.next();
                rulesProcessor.process(widget);
            }
        } catch (ExpressionException e) {
            Window.alert(e.getMessage());
        }
    }

    /**
     * Создает список необходимых процессоров для обработки на основе конфигурации правил виджета.
     *
     * @param fromBroadcast
     * @return список процессоров ({@link ru.intertrust.cm.core.gui.impl.client.rules.widget.processor.IRulesWidgetProcessor})
     */
    private List<IRulesWidgetProcessor> createAllRequiredRuleProcessors(boolean fromBroadcast) {
        List<IRulesWidgetProcessor> rulesProcessorsList = new ArrayList<>();
        RulesTypeConfig rulesTypeConfig = widget.getInitialData().getRules();

        if (rulesTypeConfig.getHideRulesTypeConfig() != null) {
            rulesProcessorsList.add(new HideRulesWidgetProcessor());
        }
        if (rulesTypeConfig.getAccessRulesTypeConfig() != null) {
            rulesProcessorsList.add(new AccessRulesWidgetProcessor());
        }
        if (rulesTypeConfig.getValueRulesTypeConfig() != null) {
            rulesProcessorsList.add(new ValueRulesWidgetProcessor());
        }
        if (rulesTypeConfig.getFilterRulesTypeConfig() != null && !fromBroadcast) {
            rulesProcessorsList.add(new ValueRulesWidgetProcessor());
        }
        //TODO: Остальные типы проавил в следующих приоритетах разработки
        return rulesProcessorsList;
    }

}
