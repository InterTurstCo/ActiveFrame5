package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.Map;

/**
 * User: IPetrov
 * Date: 09.01.14
 * Time: 11:05
 * Данные об условиях расширенного поиска
 */
public class ExtendedSearchData extends FormPluginConfig implements Dto {

    // имя доменного объекта - имя коллекции, возвращаемой при поиске
    private HashMap<String, String> targetCollectionNames;
    // условия поиска из полей формы расширенного поиска
    Map<String, WidgetState> formWidgetsData;
    // объект содержащий данные для поиска
    private SearchQuery searchQuery;
    // максимальное количество возвращаемых данных
    int maxResults;

    public HashMap<String, String> getTargetCollectionNames() {
        return targetCollectionNames;
    }

    public void setTargetCollectionNames(HashMap<String, String> targetCollectionNames) {
        this.targetCollectionNames = targetCollectionNames;
    }

    public Map<String, WidgetState> getFormWidgetsData() {
        return formWidgetsData;
    }

    public void setFormWidgetsData(Map<String, WidgetState> formWidgetsData) {
        this.formWidgetsData = formWidgetsData;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public ExtendedSearchData() {
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

}
