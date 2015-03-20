package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.search.ExtendedSearchPopupConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 16:46
 * Данные для инициализации расширенного поиска
 */
public class ExtendedSearchPluginData extends PluginData {

    // Поле для получения конфигурации областей поиска и целевых ДО: <Имя области поиска, список типов целевых ДО>
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    // Поле для получения конфигурации целевого ДО и его полей для поиска(выбираются индексируемые поля): <Тип целевого ДО, список полей>
    private HashMap<String, ArrayList<String>> searchFieldsData = new HashMap<String, ArrayList<String>>();
    // Поле для получения карты: имя доменного объекта - имя коллекции, возвращаемой при поиске
    private HashMap<String, String> targetCollectionNames;

    private Map<String, String> valueToDisplayText;

    private ExtendedSearchPopupConfig extendedSearchPopupConfig;

    public HashMap<String, String> getTargetCollectionNames() {
        return targetCollectionNames;
    }

    public void setTargetCollectionNames(HashMap<String, String> targetCollectionNames) {
        this.targetCollectionNames = targetCollectionNames;
    }

    public HashMap<String, ArrayList<String>> getSearchFieldsData() {
        return searchFieldsData;
    }

    public void setSearchFieldsData(HashMap<String, ArrayList<String>> searchFieldsData) {
        this.searchFieldsData = searchFieldsData;
    }

    public HashMap<String, ArrayList<String>> getSearchAreasData() {
        return this.searchAreasData;
    }

    public void setSearchAreasData(HashMap<String, ArrayList<String>> searchAreasData) {
        this.searchAreasData = searchAreasData;
    }

    public Map<String, String> getValueToDisplayText() {
        return valueToDisplayText;
    }

    public void setValueToDisplayText(Map<String, String> valueToDisplayText) {
        this.valueToDisplayText = valueToDisplayText;
    }

    public ExtendedSearchPopupConfig getExtendedSearchPopupConfig() {
        return extendedSearchPopupConfig;
    }

    public void setExtendedSearchPopupConfig(ExtendedSearchPopupConfig extendedSearchPopupConfig) {
        this.extendedSearchPopupConfig = extendedSearchPopupConfig;
    }
}
