package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.List;

/**
 * Данные виджета, необходимые для его отрисовки и жизненного цикла.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 16:32
 */
public abstract class WidgetData implements Dto {
    protected List<Value> fieldValues;

    public WidgetData() {
    }

    /**
     * Возвращает значения атрибута, к которому привязан виджет
     * @return значения атрибута, к которому привязан виджет
     */
    public List<Value> getFieldValues() {
        return fieldValues;
    }

    /**
     * Устанавливает значения атрибута, к которому привязан виджет
     * @param fieldValues значения атрибута, к которому привязан виджет
     */
    public void setFieldValues(List<Value> fieldValues) {
        this.fieldValues = fieldValues;
    }
}
