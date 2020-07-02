package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Класс для хранения и передачи запросов на расширенный поиск.
 * 
 * @author apirozhkov
*/
public class SearchQuery implements Dto {

    public static final String RELEVANCE = "_relevance";

    private ArrayList<String> areas = new ArrayList<String>();
    private List<String> targetObjectTypes = new ArrayList<String>();
    private HashMap<String, SearchFilter> filters = new HashMap<String, SearchFilter>();

    /**
     * Возвращает список имён добавленных областей поиска.
     * Если имена областей поиска не были добавлены или были все удалены, возвращается пустой список.
     * 
     * @return список имён областей поиска
     */
    public List<String> getAreas() {
        return areas;
    }

    /**
     * Добавляет область поиска в поисковый запрос.
     * Поиск будет проводиться только в заданных областях поиска.
     * 
     * @param area имя области поиска, заданное в конфигурации
     */
    public void addArea(String area) {
        areas.add(area);
    }

    /**
     * Добавляет набор областей поиска в поисковый запрос.
     * 
     * @param areas коллекция с именами областей поиска, заданными в конфигурации
     * @throws NullPointerException если параметр areas равен null
     */
    public void addAreas(Collection<String> areas) {
        this.areas.addAll(areas);
    }

    /**
     * Удаляет область поиска из поискового запроса.
     * 
     * @param area имя области поиска, заданное в конфигурации
     */
    public void removeArea(SearchArea area) {
        areas.remove(area);
    }

    /**
     * Очищает список добавленных областей поиска.
     * Последующее обращение к методу {@link #getAreas()} вернёт пустой список. 
     */
    public void clearAreas() {
        areas = new ArrayList<String>();
    }

    /**
     * Возвращает тип искомых объектов.
     * 
     * @return имя типа доменных объектов
     */
    public List<String> getTargetObjectTypes() {
        return targetObjectTypes;
    }

    /**
     * Устанавливает тип искомых объектов.
     * Этот тип должен быть определён как target-object-type во всех областях поиска, добавленных в этом запросе.
     * 
     * @param targetObjectType имя типа доменных объектов, определённое в конфигурации
     */
    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectTypes = new ArrayList<>();
        this.targetObjectTypes.add(targetObjectType);
    }

    /**
     * Устанавливает типы искомых объектов
     * @param targetObjectTypes
     */
    public void setTargetObjectTypes(List<String> targetObjectTypes) {
        this.targetObjectTypes = new ArrayList<>();
        this.targetObjectTypes.addAll(targetObjectTypes);
    }

    /**
     * Добавляет искомый тип
     * @param targetObjectType
     */
    public void addTargetObjectType(String targetObjectType) {
        this.targetObjectTypes.add(targetObjectType);
    }

    /**
     * Возвращает список поисковых фильтров, добавленных в запрос.
     * Если в запрос не был добавлен ни один фильтр, или же если все фильтры были удалены, возвращается пустой список.
     * 
     * @return список поисковых фильтров
     */
    public Collection<SearchFilter> getFilters() {
        return filters.values();
    }

    /**
     * Добавляет поисковый фильтр в запрос.
     * 
     * @param filter поисковый фильтр
     * @throws NullPointerException если параметр filter равен null
     */
    public void addFilter(SearchFilter filter) {
        filters.put(filter.getFieldName(), filter);
    }

    /**
     * Добавляет набор поисковых фильтров в запрос.
     * 
     * @param filters коллекция поисковых фильтров
     * @throws NullPointerException если параметр filters или хотя бы один элемент коллекции равен null
     */
    public void addFilters(Collection<SearchFilter> filters) {
        for (SearchFilter filter : filters) {
            addFilter(filter);
        }
    }

    /**
     * Удаляет поисковый фильтр из запроса.
     * 
     * @param filter удаляемый поисковый фильтр
     */
    public void removeFilter(SearchFilter filter) {
        filters.remove(filter.getFieldName());
    }

    /**
     * Очищает набор поисковых фильтров в запросе.
     * Последующее обращение к методу {@link #getFilters()} вернёт пустой список.
     */
    public void clearFilters() {
        filters = new HashMap<String, SearchFilter>();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Target type: ").append( targetObjectTypes.toString() );
        result.append("; areas: ").append(areas.toString());
        result.append("; filters: ").append(filters.toString());
        return result.toString();
    }

    public static class SearchArea {
        private final String area;
        private final String solrServerUrl;

        public SearchArea (String area, String solrServerUrl) {
            this.area = area;
            this.solrServerUrl = solrServerUrl;
        }

        public String getArea() {
            return area;
        }

        public String getSolrServerUrl() {
            return solrServerUrl;
        }

        @Override
        public boolean equals(Object obj) {
            boolean b = false;
            if (obj instanceof SearchArea) {
                SearchArea that = (SearchArea)obj;
                b = (area == null ? that.area == null : area.equals(that.area))
                        && (solrServerUrl == null ? that.solrServerUrl == null : solrServerUrl.equals(that.solrServerUrl));
            }
            return b;
        }
    }
}
