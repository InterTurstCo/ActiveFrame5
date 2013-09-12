package ru.intertrust.cm.core.config.model.gui.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Виджет - элемент пользовательского интерфейса, отобрающий специфическим образом некоторые данные в определённой части
 * разметки формы.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 14:52
 */
public class WidgetConfig implements Dto {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
