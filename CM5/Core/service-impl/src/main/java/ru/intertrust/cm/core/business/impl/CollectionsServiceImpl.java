package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CollectionsServiceDelegate;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:39 PM
 */
@Stateless
@Local(CollectionsService.class)
@Remote(CollectionsService.Remote.class)
@Interceptors({SpringBeanAutowiringInterceptor.class, CollectionsDataSourceSetter.class})
public class CollectionsServiceImpl implements CollectionsService {

    @Autowired
    @Qualifier("nonTransactionalCollectionsService")
    private CollectionsServiceDelegate nonTransactionalCollectionsService;

    @Autowired
    @Qualifier("transactionalCollectionsService")
    private CollectionsServiceDelegate transactionalCollectionsService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param offset         смещение. Если равно 0, то смещение не создается.
     * @param limit          максимальное количество возвращаемых доменных объектов. Если указано 0, то не ограничивается количество
     * @param dataSource
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder,
                                                       List<? extends Filter> filters, int offset, int limit,
                                                       DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollection(collectionName, sortOrder, filters, offset, limit);
        } else {
            return nonTransactionalCollectionsService.findCollection(collectionName, sortOrder, filters, offset, limit);
        }
    }

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param dataSource
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<? extends Filter> filters, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollection(collectionName, sortOrder, filters);
        } else {
            return nonTransactionalCollectionsService.findCollection(collectionName, sortOrder, filters);
        }
    }

    /**
     * Возвращает заданную коллекцию, упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param dataSource
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollection(collectionName, sortOrder);
        } else {
            return nonTransactionalCollectionsService.findCollection(collectionName, sortOrder);
        }
    }

    /**
     * Проверяет, пуста ли коллекция.
     *
     * @param collectionName название коллекции
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param dataSource
     * @return пустая ли коллекция.
     */
    @Override
    public boolean isCollectionEmpty(String collectionName, List<? extends Filter> filters, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.isCollectionEmpty(collectionName, filters);
        } else {
            return nonTransactionalCollectionsService.isCollectionEmpty(collectionName, filters);
        }
    }

    /**
     * Возвращает заданную коллекцию
     *
     * @param collectionName название коллекции
     * @param dataSource
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollection(collectionName);
        } else {
            return nonTransactionalCollectionsService.findCollection(collectionName);
        }
    }

    /**
     * Возвращает коллекцию по запросу
     *
     * @param query      запрос
     * @param offset     смещение. Если равно 0, то смещение не создается.
     * @param limit      максимальное количество возвращаемых доменных объектов. Если равно 0, то не ограничивается
     *                   количество.
     * @param dataSource
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollectionByQuery(query, offset, limit);
        } else {
            return nonTransactionalCollectionsService.findCollectionByQuery(query, offset, limit);
        }
    }

    /**
     * Возвращает коллекцию по запросу
     *
     * @param query      запрос
     * @param dataSource
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollectionByQuery(query);
        } else {
            return nonTransactionalCollectionsService.findCollectionByQuery(query);
        }
    }

    /**
     * Возвращает количество элементов заданной коллекции, отфильтрованной согласно списку фильтров
     *
     * @param collectionName название коллекции
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param dataSource
     * @return число элементов заданной коллекции
     */
    @Override
    public int findCollectionCount(String collectionName, List<? extends Filter> filters, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollectionCount(collectionName, filters);
        } else {
            return nonTransactionalCollectionsService.findCollectionCount(collectionName, filters);
        }
    }

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     * Используются нумерованные параметры вида {0}, {1} и т.д.
     * При этом переданные параметры должны идти в том же порядке (в List<Value> params),
     * в котором указаны их индексы в SQL запросе.
     *
     * @param query      SQL запрос
     * @param params     параметры запроса
     * @param offset     смещение. Если равно 0, то смещение не создается.
     * @param limit      ограничение количества возвращенных доменных объектов. Если равно 0, то не ограничивается
     *                   количество.
     * @param dataSource
     * @return результат поиска в виде {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params, int offset, int limit, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollectionByQuery(query, params, offset, limit);
        } else {
            return nonTransactionalCollectionsService.findCollectionByQuery(query, params, offset, limit);
        }
    }

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     *
     * @param query      SQL запрос
     * @param params     параметры запроса
     * @param dataSource
     * @return результат поиска в виде {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params, DataSourceContext dataSource) {
        if (DataSourceContext.MASTER.equals(dataSource)) {
            return transactionalCollectionsService.findCollectionByQuery(query, params);
        } else {
            return nonTransactionalCollectionsService.findCollectionByQuery(query, params);
        }
    }

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param offset         смещение. Если равно 0, то смещение не создается.
     * @param limit          максимальное количество возвращаемых доменных объектов. Если указано 0, то не ограничивается количество
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<? extends Filter> filters, int offset, int limit) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            return nonTransactionalCollectionsService.findCollection(collectionName, sortOrder, filters, offset, limit);
        } else {
            return transactionalCollectionsService.findCollection(collectionName, sortOrder, filters, offset, limit);
        }
    }

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<? extends Filter> filters) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            return nonTransactionalCollectionsService.findCollection(collectionName, sortOrder, filters);
        } else {
            return transactionalCollectionsService.findCollection(collectionName, sortOrder, filters);
        }
    }

    /**
     * Возвращает заданную коллекцию, упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            return nonTransactionalCollectionsService.findCollection(collectionName, sortOrder);
        } else {
            return transactionalCollectionsService.findCollection(collectionName, sortOrder);
        }
    }

    /**
     * Проверяет, пуста ли коллекция.
     *
     * @param collectionName название коллекции
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @return пустая ли коллекция.
     */
    @Override
    public boolean isCollectionEmpty(String collectionName, List<? extends Filter> filters) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            return nonTransactionalCollectionsService.isCollectionEmpty(collectionName, filters);
        } else {
            return transactionalCollectionsService.isCollectionEmpty(collectionName, filters);
        }
    }

    /**
     * Возвращает заданную коллекцию
     *
     * @param collectionName название коллекции
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            return nonTransactionalCollectionsService.findCollection(collectionName);
        } else {
            return transactionalCollectionsService.findCollection(collectionName);
        }
    }

    /**
     * Возвращает коллекцию по запросу
     *
     * @param query  запрос
     * @param offset смещение. Если равно 0, то смещение не создается.
     * @param limit  максимальное количество возвращаемых доменных объектов. Если равно 0, то не ограничивается
     *               количество.
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit) {
        return transactionalCollectionsService.findCollectionByQuery(query, offset, limit);
    }

    /**
     * Возвращает коллекцию по запросу
     *
     * @param query запрос
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query) {
        return transactionalCollectionsService.findCollectionByQuery(query);
    }

    /**
     * Возвращает количество элементов заданной коллекции, отфильтрованной согласно списку фильтров
     *
     * @param collectionName название коллекции
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @return число элементов заданной коллекции
     */
    @Override
    public int findCollectionCount(String collectionName, List<? extends Filter> filters) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        if (collectionConfig.isUseClone()) {
            return nonTransactionalCollectionsService.findCollectionCount(collectionName, filters);
        } else {
            return transactionalCollectionsService.findCollectionCount(collectionName, filters);
        }
    }

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     * Используются нумерованные параметры вида {0}, {1} и т.д.
     * При этом переданные параметры должны идти в том же порядке (в List<Value> params),
     * в котором указаны их индексы в SQL запросе.
     *
     * @param query  SQL запрос
     * @param params параметры запроса
     * @param offset смещение. Если равно 0, то смещение не создается.
     * @param limit  ограничение количества возвращенных доменных объектов. Если равно 0, то не ограничивается
     *               количество.
     * @return результат поиска в виде {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params, int offset, int limit) {
        return transactionalCollectionsService.findCollectionByQuery(query, params, offset, limit);
    }

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     *
     * @param query  SQL запрос
     * @param params параметры запроса
     * @return результат поиска в виде {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params) {
        return transactionalCollectionsService.findCollectionByQuery(query, params);
    }
}
