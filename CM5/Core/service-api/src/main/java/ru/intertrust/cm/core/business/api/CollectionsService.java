package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:40 PM
 */
public interface CollectionsService {

    /**
     * Возвращает коллекцию, отфильтрованную и упорядоченную согласно критериям
     *
     * @param collectionName название коллекции
     * @param filters фильтры
     * @param sortOrder порядок сортировки коллекции
     * @param limit максимальное количесвто возвращаемых доменных объектов
     * @return коллекцию
     */
    IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filters, SortOrder sortOrder, int offset, int limit);

    /**
     * Возвращает коллекцию, упорядоченную согласно заданному порядку
     *
     * @param collectionName название коллекции
     * @param filters фильтры
     * @return коллекцию
     */
    int findCollectionCount(String collectionName, List<Filter> filters);

}
