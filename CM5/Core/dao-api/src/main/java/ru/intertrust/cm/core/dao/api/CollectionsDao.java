package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:56 PM
 */
public interface CollectionsDao {

    /**
     * Поиск коллекции доменных объектов, используя фильтры и сортировку
     *
     *
     * @param collectionName конфигурация коллекции
     * @param filterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     * @return
     */
    IdentifiableObjectCollection findCollection(String collectionName,
                                                List<Filter> filterValues, SortOrder sortOrder, int offset, int limit);

    /**
     * Поиск количества записей в коллекции доменных объектов используя фильтры
     *
     *
     * @param collectionName конфигурация коллекции
     * @return
     */
    int findCollectionCount(String collectionName, List<Filter> filterValues);

}
