package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Виджет - элемент пользовательского интерфейса, отобрающий специфическим образом некоторые данные в определённой части
 * разметки формы.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 14:52
 */
public abstract class WidgetDefinition implements Dto {
    private String id;
    private String fieldPath;
}
