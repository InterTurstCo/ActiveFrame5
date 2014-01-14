package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.exception.DaoException;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.*;

/**
 * Кеш уровня транзакции для системных коллекций
 * Для того, чтобы задействовать кеш для нужной коллекции в collections.xml нужно добавить атрибут
 * transaction-cache="enabled"
 * например,
 * <collection name="PersonByLogin" idField="id" transaction-cache="enabled">
 */
@Service
public class CollectionsCacheServiceImpl {

    @Resource
    private TransactionSynchronizationRegistry txReg;

    /**
     * Ключ для идентификации коллекции в кеше.
     * Состоит из параметров запроса
     */
    public static class CollectionsCacheKey{
        private String collectionName;
        private List<? extends Filter> filterValues;
        private SortOrder sortOrder;
        private int offset;
        private int limit;

        public CollectionsCacheKey(String collectionName, List<? extends Filter> filterValues,
                                   SortOrder sortOrder, int offset, int limit) {
            this.collectionName = collectionName;
            this.filterValues = filterValues;
            this.sortOrder = sortOrder;
            this.offset = offset;
            this.limit = limit;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CollectionsCacheKey another = (CollectionsCacheKey) o;

            if (limit != another.limit) return false;
            if (offset != another.offset) return false;
            if (!collectionName.equals(another.collectionName)) return false;
            if (filterValues != null ? !filterValues.equals(another.filterValues) : another.filterValues != null)
                return false;
            if (sortOrder != null ? !sortOrder.equals(another.sortOrder) : another.sortOrder != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = filterValues != null ? filterValues.hashCode() : 0;
            result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
            result = 31 * result + offset;
            result = 31 * result + limit;
            result = 31 * result + collectionName.hashCode();
            return result;
        }
    }


    /**
     * Кеширование коллекции в транзакционный кеш.
     * @param collection коллекция соответствующая запросу для внесения в кеш
     * @param collectionName конфигурация коллекции
     * @param filterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     */
    public void putCollectionToCache(IdentifiableObjectCollection collection, String collectionName,
                               List<? extends Filter> filterValues,
                               SortOrder sortOrder, int offset, int limit) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        if (collection != null) {
            getTxReg().putResource(new CollectionsCacheKey(collectionName, filterValues, sortOrder, offset, limit),
                    collection);
        }
    }


    /**
     * Возвращает коллекцию из кеш
     * @param collectionName конфигурация коллекции
     * @param filterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @return коллекцию из кеш, null - если не найдено в кэше
     */
    public IdentifiableObjectCollection getCollectionFromCache(String collectionName,
                                               List<? extends Filter> filterValues,
                                               SortOrder sortOrder, int offset, int limit) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        return (IdentifiableObjectCollection) getTxReg().getResource(
                new CollectionsCacheKey(collectionName, filterValues, sortOrder, offset, limit));
    }





    private TransactionSynchronizationRegistry getTxReg() {
        if (txReg == null) {
            try {
                txReg = (TransactionSynchronizationRegistry) new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            } catch (NamingException e) {
                throw new DaoException(e);
            }
        }
        return txReg;
    }
}