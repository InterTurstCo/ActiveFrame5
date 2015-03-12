package ru.intertrust.cm.core.gui.model.filters;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.Map;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.11.2014
 *         Time: 19:31
 */
public class ComplexFiltersParams implements Dto {
    private Id rootId;
    private String inputFilterValue;
    private String inputFilterName;
    private Map<WidgetIdComponentName, WidgetState> widgetValuesMap;

    public ComplexFiltersParams() {
    }

    public ComplexFiltersParams(Id rootId) {
        this.rootId = rootId;
    }

    public ComplexFiltersParams(Id rootId, Map<WidgetIdComponentName, WidgetState> widgetValuesMap) {
        this(rootId);
        this.widgetValuesMap = widgetValuesMap;
    }

    public ComplexFiltersParams(Id rootId, String inputFilterValue, String inputFilterName,
                                Map<WidgetIdComponentName, WidgetState> widgetValuesMap) {
        this(rootId, widgetValuesMap);
        this.inputFilterValue = inputFilterValue;
        this.inputFilterName = inputFilterName;
    }

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public void setWidgetValuesMap(Map<WidgetIdComponentName, WidgetState> widgetValuesMap) {
        this.widgetValuesMap = widgetValuesMap;
    }

    public String getInputFilterValue() {
        return inputFilterValue;
    }

    public void setInputFilterValue(String inputFilterValue) {
        this.inputFilterValue = inputFilterValue;
    }

    public String getInputFilterName() {
        return inputFilterName;
    }

    public void setInputFilterName(String inputFilterName) {
        this.inputFilterName = inputFilterName;
    }

    public Map.Entry<WidgetIdComponentName, WidgetState> getWidgetValue(String widgetId){
        if(widgetValuesMap == null){
            return null;
        }
        Set<Map.Entry<WidgetIdComponentName, WidgetState>> widgetValues = widgetValuesMap.entrySet();
        for (Map.Entry<WidgetIdComponentName, WidgetState> widgetValue : widgetValues) {
            if(widgetValue.getKey().getWidgetId().equalsIgnoreCase(widgetId)){
                return widgetValue;
            }
        }
        return null;
    }

}
