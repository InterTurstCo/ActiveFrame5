package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.MarkupConfig;
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
public class Form implements Dto {
    private String name;
    private boolean editable = true;
    private MarkupConfig markup;
    private Map<String, WidgetData> widgetDataMap;
    private FormObjects objects;
    private boolean debug = false;
    /**
     * Конструктор по умолчанию.
     */
    public Form() {
    }

    /**
     * Конструктор формы по названию и разметке.
     * @param name название формы
     * @param markup разметка формы
     */
    public Form(String name, MarkupConfig markup) {
        this.name = name;
        this.markup = markup;
    }

    /**
     * Конструктор формы по названию, разметке и данным виджетов.
     * @param name название формы
     * @param markup разметка формы
     * @param widgetDataMap данные виджетов
     */
    public Form(String name, MarkupConfig markup, Map<String, WidgetData> widgetDataMap) {
        this(name, markup);
        this.widgetDataMap = widgetDataMap;
    }

    public Form(String name, MarkupConfig markup, Map<String, WidgetData> widgetDataMap, FormObjects objects, boolean debug) {
        this.name = name;
        this.markup = markup;
        this.objects = objects;
        this.widgetDataMap = widgetDataMap;
        this.debug = debug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setWidgetDataMap(Map<String, WidgetData> widgetDataMap) {
        this.widgetDataMap = widgetDataMap;
    }

    /**
     * Возвращает разметку формы.
     * @return разметку формы
     */
    public MarkupConfig getMarkup() {
        return markup;
    }

    /**
     * Устанавливает разметку формы.
     * @param markup разметка формы
     */
    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
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

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
