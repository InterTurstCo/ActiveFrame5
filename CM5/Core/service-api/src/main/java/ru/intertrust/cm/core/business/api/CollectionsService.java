package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:40 PM
 */
public interface CollectionsService extends CollectionsServiceDelegate {

    interface Remote extends CollectionsService {
    }

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param limit          максимальное количество возвращаемых доменных объектов. Если указано 0, то не ограничивается количество 
     * @param offset         смещение. Если равно 0, то смещение не создается.
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder,
            List<? extends Filter> filters, int offset, int limit, DataSourceContext dataSource);

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder,
            List<? extends Filter> filters, DataSourceContext dataSource);

    /**
     * Возвращает заданную коллекцию, упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, DataSourceContext dataSource);

    /**
     * Проверяет, пуста ли коллекция.
     * @param collectionName название коллекции
     * @param filters список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return пустая ли коллекция.
     */
    boolean isCollectionEmpty(String collectionName, List<? extends Filter> filters, DataSourceContext dataSource);
    
    /**
     * Возвращает заданную коллекцию
     *
     * @param collectionName название коллекции
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, DataSourceContext dataSource);

    /**
     * Возвращает коллекцию по запросу
     * @param query запрос
     * @param limit максимальное количество возвращаемых доменных объектов. Если равно 0, то не ограничивается
     *            количество.
     * @param offset смещение. Если равно 0, то смещение не создается.
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit, DataSourceContext dataSource);


    /**
     * Возвращает коллекцию по запросу
     * @param query запрос
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query, DataSourceContext dataSource);

    /**
     * Возвращает количество элементов заданной коллекции, отфильтрованной согласно списку фильтров
     *
     * @param collectionName название коллекции
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return число элементов заданной коллекции
     */
    int findCollectionCount(String collectionName, List<? extends Filter> filters, DataSourceContext dataSource);

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     * Используются нумерованные параметры вида {0}, {1} и т.д.
     * При этом переданные параметры должны идти в том же порядке (в List<Value> params),
     * в котором указаны их индексы в SQL запросе.
     * @param query SQL запрос
     * @param params параметры запроса
     * @param limit ограничение количества возвращенных доменных объектов. Если равно 0, то не ограничивается
     *            количество.
     * @param offset смещение. Если равно 0, то смещение не создается.
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return результат поиска в виде {@link IdentifiableObjectCollection}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query,
            List<? extends Value> params, int offset, int limit, DataSourceContext dataSource);

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     * @param query SQL запрос
     * @param params параметры запроса
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return результат поиска в виде {@link IdentifiableObjectCollection}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params, DataSourceContext dataSource);

}
