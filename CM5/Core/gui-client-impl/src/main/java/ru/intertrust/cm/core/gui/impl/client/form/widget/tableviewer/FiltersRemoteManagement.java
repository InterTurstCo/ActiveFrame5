package ru.intertrust.cm.core.gui.impl.client.form.widget.tableviewer;

import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 12.07.2016
 * Time: 11:54
 * To change this template use File | Settings | File and Code Templates.
 */
public interface FiltersRemoteManagement {
    /**
     * Возвращает текущие дополнительные фильтры
     * @return
     */
    CollectionExtraFiltersConfig getCollectionExtraFilters();

    /**
     * Применяет указанные дополнительные фильтры
     * @param config
     */
    void applyCollectionExtraFilters(CollectionExtraFiltersConfig config);

    /**
     * Сброс фильтров в колонках
     */
    void resetColumnFilters();

    /**
     * Возвращает текущее состояние фильтров в колонках
     * @return
     */
    Map<String, List<String>> getColumnFiltersMap();

    /**
     * Применить фильтры к колонкам
     * @param filtersMap
     */
    void applyColumnFilters(Map<String, List<String>> filtersMap);
}
