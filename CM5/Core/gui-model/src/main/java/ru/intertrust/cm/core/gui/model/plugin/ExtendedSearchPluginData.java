package ru.intertrust.cm.core.gui.model.plugin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 16:46
 * Данные для инициализации расширенного поиска
 */
public class ExtendedSearchPluginData extends PluginData {

    // Поле для получения конфигурации областей поиска и целевых ДО
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    // Поле для получения конфигурации целевого ДО и его полей для поиска(выбираются индексируемые поля)
    private HashMap<String, ArrayList<String>> searchFieldsData = new HashMap<String, ArrayList<String>>();

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

    public ExtendedSearchPluginData() {
    }

}
