package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

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
    private Map<String, WidgetState> widgetStateMap;
    private FormObjects objects;

    /**
     * Конструктор по умолчанию.
     */
    public FormState() {
    }

    public FormState(String name, Map<String, WidgetState> widgetStateMap, FormObjects objects) {
        this.name = name;
        this.objects = objects;
        this.widgetStateMap = widgetStateMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWidgetStateMap(Map<String, WidgetState> widgetStateMap) {
        this.widgetStateMap = widgetStateMap;
    }

    /**
     * Устанавливает данные конкретного виджета. Если данные уже установлены, замещает их.
     * @param widgetId идентификатор виджета
     * @param widgetState данные виджета
     */
    public void setWidgetState(String widgetId, WidgetState widgetState) {
        widgetStateMap.put(widgetId, widgetState);
    }

    /**
     * Возвращает данные виджета.
     * @param widgetId идентификатор виджета
     * @return данные виджета
     */
    public WidgetState getWidgetState(String widgetId) {
        return widgetStateMap.get(widgetId);
    }

    /**
     * Возвращает данные всех виджетов формы. Возвращаемая "карта" не является защищённой от записи - изменения в ней
     * повлекут за собой изменения в самом объекте формы.
     * @return данные всех виджетов формы в виде "карты", ключом которой является идентификатор виджета
     */
    public Map<String, WidgetState> getFullWidgetsState() {
        return widgetStateMap;
    }

    public FormObjects getObjects() {
        return objects;
    }

    public void setObjects(FormObjects objects) {
        this.objects = objects;
    }

}
