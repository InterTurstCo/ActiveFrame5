package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import ru.intertrust.cm.core.business.api.CollectionsServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

/**
 * Нетранзакционная версия {@link ru.intertrust.cm.core.business.api.CollectionsServiceDelegate}
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:39 PM
 */
@Stateless
@Local(CollectionsServiceDelegate.class)
@Remote(CollectionsServiceDelegate.Remote.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NonTransactionalCollectionsServiceImpl extends CollectionsServiceBaseImpl implements CollectionsServiceDelegate {

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
        return super.findCollection(collectionName, sortOrder, filters, offset, limit);
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
        return super.findCollection(collectionName, sortOrder, filters);
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
        return super.findCollection(collectionName, sortOrder);
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
        return super.isCollectionEmpty(collectionName, filters);
    }

    /**
     * Возвращает заданную коллекцию
     *
     * @param collectionName название коллекции
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName) {
        return super.findCollection(collectionName);
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
        return super.findCollectionByQuery(query, offset, limit);
    }

    /**
     * Возвращает коллекцию по запросу
     *
     * @param query запрос
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query) {
        return super.findCollectionByQuery(query);
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
        return super.findCollectionCount(collectionName, filters);
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
        return super.findCollectionByQuery(query, params, offset, limit);
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
        return super.findCollectionByQuery(query, params);
    }
}
