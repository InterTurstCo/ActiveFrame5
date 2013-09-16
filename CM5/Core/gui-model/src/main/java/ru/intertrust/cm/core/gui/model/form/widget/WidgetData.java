package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Данные виджета, необходимые для его отрисовки и жизненного цикла.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 16:32
 */
public abstract class WidgetData implements Dto {
    public abstract String getComponentName();
}
