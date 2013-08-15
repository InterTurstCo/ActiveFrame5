package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.exception.DaoException;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.*;

/**
 * Транзакционное кеширование DomainObject.
 * Сервис предоставляет кеширование DomainObject транзакционном кеше.
 * В транзакционном кеше хранится запись следующего вида
 * id = node
 * node - содержит:
 * 1. список id элементов (дочерних), связаннях с указанным id,
 * например, внешние ключи, parent поле.
 * Список ид связан с ключевой фразой, например
 * таким образом один и тот же Id может принадлежать нескольким веткам именованные ключевыми фразами.
 * Список можно менять динамически, также можно изменять динамически экземпляр DomainObject в кеше
 * Изменение экземпляра, распостраняется на все элементы списка id,
 * т.е. рассмотрим пример
 * ID1 = родительский ид, включает в себя список дочерних id {ID2, ID3}
 * установим связь между ид и экземпляром
 * ID1 - DO1
 * ID2 - DO2
 * ID3 - DO3
 * Выполним запрос, получить дочерний объект из родительского списка.
 * Рассмотрим пошагово действия сервиса.
 * 1. Получаем список ID2, ID3
 * 2. Запрашиваем кеш на получение объекта с ID2, ID3
 * 3. Получаем список DO1, DO2
 *
 * Недостаток в том, что при большом списке, выполняется итерация по списку для запроса доменного объекта из кеш по Id и
 * создание списка запрашиваемых объектов.
 * Сам доступ к доменному объекту выполняется быстро, так как используется hash
 *
 */
@Service
public class DomainObjectCacheServiceImpl {

    @Resource
    private TransactionSynchronizationRegistry txReg;

    static private class DomainObjectNode {
        //key - ключевая фраза, value - упорядоченный список
        private Map<String, LinkedHashSet<Id>> childDomainObjectIdMap = new HashMap<>();
        //clone доменного объекта
        private DomainObject domainObject;

        private DomainObjectNode() {
        }

        //клонированный объект в кеш
        private void setDomainObject(DomainObject domainObject) throws CloneNotSupportedException {
            //deep clone
            this.domainObject = domainObject == null
                    ? null
                    : (DomainObject) SerializationUtils.deserialize(SerializationUtils.serialize(domainObject));
        }

        /**
         * добавляем идентификаторы связанных (дочерних) объектов
         * составной ключ - связывает список id дочерних объектов с метками клиента,
         * например, DomainObjectTypeName:field
         * первым элементом ключа (неявным параметром) будет название типа доменного объекта
         * разделитель ':'
         * @param ids идентификаторы связанных (дочерних) объектов
         * @param key ключевая фраза
         */
        private void addChildDocumentObjectIds(List<Id> ids, String ... key) {
            String complexKey = generateKey(key);
            LinkedHashSet idSet = childDomainObjectIdMap.get(complexKey);
            if (idSet == null) {
                idSet = new LinkedHashSet();
                childDomainObjectIdMap.put(complexKey, idSet);
            }
            idSet.addAll(ids);
        }


        /**
         *
         * @param id дочернего элемента
         * @param key ключевая фраза, первым элементом ключа (неявным параметром) будет название типа доменного объекта
         */
        private void addChildDocumentObjectId(Id id, String ... key) {
            addChildDocumentObjectIds(Arrays.asList(id), key);
        }

        /**
         * @param id дочернего элемента, удаляется элемент из списка
         */
        private void delChildDocumentObjectId(Id id) {
            for (Set<Id> ids : childDomainObjectIdMap.values()) {
                ids.remove(id);
            }
        }

        /**
         *
         * @param keys - ключевая фраза, первым элементом ключа (неявным параметром) будет название типа доменного объекта
         * @return возвращает список id связанных (дочерних, через внешние ключи) доменных объектов,
         * null - если не существует списка для указанной ключевой фразы
         */
        private List<Id> getChildDomainObjectIds(String... keys) {
            String complexKey = generateKey(keys);
            return childDomainObjectIdMap.containsKey(complexKey)
                    ? new ArrayList(childDomainObjectIdMap.get(complexKey))
                    : null;
        }

        /**
         *
         * @return клон доменного объекта
         */
        private DomainObject getDomainObject() {
            return domainObject;
        }

        /**
         * Создает составной ключ (ключевая фраза)
         * первым элементом ключа (неявным параметром) будет название типа доменного объекта
         * разделитель ':'
         * @param key ключевая фраза, первым элементом ключа (неявным параметром) будет название типа доменного объекта
         * @return составной ключ (ключевая фраза)
         */

        static String generateKey(String ... key) {
            if (key.length == 1) {
                return key[0];
            }
            StringBuilder sb = new StringBuilder();
            for (String k : key) {
                if (sb.length() > 0) {
                    sb.append(":");
                }
                sb.append(k);
            }
            return sb.toString();
        }

        /**
         * Обнуляет domainObject
         * Очищает список дочерних элементов
         */
        private void clear() {
            domainObject = null;
            childDomainObjectIdMap.clear();
        }
    }

    /**
     *
     * @param id доменного объекта
     * @return контейнер (узел) доменного объекта
     */
    private DomainObjectNode getDomainObjectNode(Id id) {
        DomainObjectNode don = (DomainObjectNode) getTxReg().getResource(id);
        if (don == null) {
            don = new DomainObjectNode();
            getTxReg().putResource(id, don);
        }
        return don;
    }

    /**
     *
     * @param id доменного объекта
     * @return true - если существует контейнер суказанным id
     */
    private boolean isEmptyDomainObjectNode(Id id) {
        return getTxReg().getResource(id) == null;
    }

    /**
     *
     * @param dobj доменный объект
     * @return список родительских Id
     */
    private Map<String, Id> getRefIdAndFieldMap(DomainObject dobj) {
        Map<String, Id> ret = new HashMap();
        for (String fn : dobj.getFields()) {
            Value v = dobj.getValue(fn);
            if (v.get() != null && v instanceof ReferenceValue) {
                ret.put(fn, (Id) v.get());
            }
        }
        if (dobj.getParent() != null) {
            ret.put("parent", dobj.getParent());
        }
        return ret;
    }

    /**
     * Кеширование DomainObject, в кеш создается клон DomainObject
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    public Id putObjectToCache(DomainObject dobj) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        try {
            getDomainObjectNode(dobj.getId()).setDomainObject(dobj);
        } catch (CloneNotSupportedException e) {
            return null;
        }
        Map<String, Id> idMap = getRefIdAndFieldMap(dobj);
        for (Map.Entry<String, Id> ent : idMap.entrySet()) {
            DomainObjectNode parentDon = getDomainObjectNode(ent.getValue());
            parentDon.addChildDocumentObjectId(dobj.getId(), dobj.getTypeName(), ent.getKey());
        }
        return dobj.getId();
    }

    /**
     * Кеширование списка DomainObject, в кеш создается клон DomainObject
     * @param dobjs список кешируемых объектов
     * @return список Id кешируемых объектов
     */
    public List<Id> putObjectToCache(List<DomainObject> dobjs) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        List<Id> ids = new ArrayList<>();
        for (DomainObject dobj : dobjs) {
            putObjectToCache(dobj);
            ids.add(putObjectToCache(dobj));
        }
        return ids;
    }

    /**
     * Кеширование списка DomainObject, для указанного родителя с ключевой фразой
     * @param parentId - родительский Id
     * @param dobjs список кешируемых объектов
     * @param key ключевая фраза
     * @return список Id кешируемых объектов
     */
    public List<Id> putObjectToCache(Id parentId, List<DomainObject> dobjs, String ... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        List<Id> ids = putObjectToCache(dobjs);
        getDomainObjectNode(parentId).addChildDocumentObjectIds(ids, key);
        return ids;
    }

    /**
     * Возвращает закешированный объект
     * @param id - Id кешированного объекта
     * @return закешированный объект
     */
    public DomainObject getObjectToCache(Id id) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        return isEmptyDomainObjectNode(id) ? null : getDomainObjectNode(id).getDomainObject();
    }

    /**
     * Возвращает список закешированных объектов
     * @param ids - список Id закешированных объектов
     * @return список закешированных объектов,
     * null - если список пустой
     */
    public List<DomainObject> getObjectToCache(List<? extends Id> ids) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        List<DomainObject> dobjs = new ArrayList<>();
        for (Id id : ids) {
            DomainObject dobj = getObjectToCache(id);
            if (dobj != null) {
                dobjs.add(dobj);
            }
        }
        return dobjs.size() == 0 ? null : dobjs;
    }

    /**
     * Возвращает список закешированных объектов
     * @param parentId - родительский Id
     * @param key ключевая фраза
     * @return список закешированных объектов,
     * null - если список не существует для указанной ключевой фразы
     */
    public List<DomainObject> getObjectToCache(Id parentId, String ... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        final String complexKey = DomainObjectNode.generateKey(key);
        DomainObjectNode parentDon = isEmptyDomainObjectNode(parentId) ? null : getDomainObjectNode(parentId);
        if (parentDon == null) {
            return null;
        }

        List<Id> ids = parentDon.getChildDomainObjectIds(complexKey);
        if (ids == null) {
            return null;
        }

        List<DomainObject> ret = new ArrayList<>();
        for (Id id : ids) {
            DomainObjectNode don = isEmptyDomainObjectNode(id) ? null : getDomainObjectNode(id);
            if (don != null && don.getDomainObject() != null) {
                ret.add(don.getDomainObject());
            }
        }
        return ret;
    }

    /**
     * Удаляет закешированный объет из транзакционного кеша
     * @param id - кешированного объекта
     */
    public void removeObjectFromCache(Id id) {
        if (!isEmptyDomainObjectNode(id)) {
            DomainObject dobj = isEmptyDomainObjectNode(id) ? null : getDomainObjectNode(id).getDomainObject();
            if (dobj != null) {
                Map<String, Id> idMap = getRefIdAndFieldMap(dobj);
                for (Map.Entry<String, Id> ent : idMap.entrySet()) {
                    DomainObjectNode parentDon = isEmptyDomainObjectNode(ent.getValue())
                            ? null : getDomainObjectNode(ent.getValue());
                    if (parentDon != null) {
                        parentDon.delChildDocumentObjectId(id);
                    }
                }
            }
            getDomainObjectNode(id).clear();
        }

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