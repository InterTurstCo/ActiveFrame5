package ru.intertrust.cm.core.gui.impl.client.rules.widget.processor;

import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;

/**
 * Интерфейс процессора по обработке правил, применяемых к виджету.<br>
 * <br>
 * Created by Myskin Sergey on 30.06.2020.
 */
public interface IRulesWidgetProcessor<T extends BaseWidget> {

    /**
     * Выполняет действия над виджетом по обработке правила процессором.
     *
     * @param widget объект виджета, содержащий данные его состояния, а так же методы действий, с помощью которых эти данные можно обработать/изменить.
     */
    void process(T widget) throws ExpressionException;

    /**
     * Возвращает порядок обработки правила виджета, чем цифра меньше, тем оно раньше (приоритетнее) будет выполнено.
     *
     * @return порядок обработки правила
     */
    int getOrder();

}
