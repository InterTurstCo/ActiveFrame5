package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

import java.util.Map;

/**
 * Данные формы, необходимые для отображения и функционирования её разметки и виджетов. По умолчанию форма является
 * редактируемой.
 *
 * @author Denis Mitavskiy
 *         Date: 12.09.13
 *         Time: 18:18
 */
public class FormState implements Dto {
    private String name;
    private Map<String, WidgetData> widgetDataMap;
    private FormObjects objects;

    /**
     * Конструктор по умолчанию.
     */
    public FormState() {
    }

    /**
     * Конструктор формы по названию и разметке.
     * @param name название формы
     */
    public FormState(String name) {
        this.name = name;
    }

    public FormState(String name, Map<String, WidgetData> widgetDataMap, FormObjects objects) {
        this.name = name;
        this.objects = objects;
        this.widgetDataMap = widgetDataMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWidgetDataMap(Map<String, WidgetData> widgetDataMap) {
        this.widgetDataMap = widgetDataMap;
    }

    /**
     * Устанавливает данные конкретного виджета. Если данные уже установлены, замещает их.
     * @param widgetId идентификатор виджета
     * @param widgetData данные виджета
     */
    public void setWidgetData(String widgetId, WidgetData widgetData) {
        widgetDataMap.put(widgetId, widgetData);
    }

    /**
     * Возвращает данные виджета.
     * @param widgetId идентификатор виджета
     * @return данные виджета
     */
    public WidgetData getWidgetData(String widgetId) {
        return widgetDataMap.get(widgetId);
    }

    /**
     * Возвращает данные всех виджетов формы. Возвращаемая "карта" не является защищённой от записи - изменения в ней
     * повлекут за собой изменения в самом объекте формы.
     * @return данные всех виджетов формы в виде "карты", ключом которой является идентификатор виджета
     */
    public Map<String, WidgetData> getFullWidgetData() {
        return widgetDataMap;
    }

    public FormObjects getObjects() {
        return objects;
    }

    public void setObjects(FormObjects objects) {
        this.objects = objects;
    }

}
