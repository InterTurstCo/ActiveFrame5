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
 * Сервис предоставляет кеширование DomainObject в транзакционном кеше.
 * В транзакционном кеше хранится запись следующего вида
 * Id = DomainObjectNode, где
 * Id - идентификатор доменного объекта
 * DomainObjectNode - контейнер доменного объекта
 * DomainObjectNode - содержит:
 * 1. список id дочерних доменных объектов, связанных с родительским id, через внешние поля, parent поле.
 * Список id дочерних доменных объектов привязан к ключевой фразе,
 * например, был выполнен поиск дочерних объектов с заданным предикатом,
 * таким образом один и тот же Id дочернего объекта может принадлежать нескольким веткам именованных ключевыми фразами.
 * Список может меняться динамически, также можно изменять динамически экземпляр DomainObject в кеше
 * Модификация дочернего объекта, распостраняется на все родительские объекты,
 * т.е. рассмотрим пример
 * ID1 = родительский Id, включает в себя список дочерних id {ID2, ID3} с фразой QUERY1
 * установим связь между Id и DomainObjectNode
 * ID1 - DO1
 * ID2 - DO2
 * ID3 - DO3
 * Выполним запрос, получения дочернего объекта из родительского списка по ключевой фразе QUERY1.
 * Рассмотрим пошагово действия сервиса.
 * 1. Получаем список ID2, ID3 для ключевой фразы QUERY1.
 * 2. Запрашиваем кеш на получение DocumentObjectNode для ID2, ID3.
 * 3. Получаем список DO1, DO2
 * 4. из DO1 получаем DomainObject и т.д.
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

    /**
     * Контейнер доменного объекта.
     * Добавляется в транзакционный кеш в соответствии с заданным ключом Id
     * Содержит список Id дочерних (связанных по reference field или parent) доменных объектов,
     * клон доменного объекта
     */
    static private class DomainObjectNode {
        //key - ключевая фраза, value - упорядоченный список Id
        private Map<String, LinkedHashSet<Id>> childDomainObjectIdMap = new HashMap<>();
        //множество Id родительский объектов
        private Set<Id> parentDomainObjectIdSet = new HashSet<>();
        //clone доменного объекта
        private DomainObject domainObject;

        private DomainObjectNode() {
        }

        /**
         * Клонирует и сохраняет DomainObject
         * @param domainObject объект, который добавляется в транзакционный кеш
         */
        private void setDomainObject(DomainObject domainObject) {
            //deep clone
            this.domainObject = domainObject == null
                    ? null
                    : (DomainObject) SerializationUtils.deserialize(SerializationUtils.serialize(domainObject));
        }

        /**
         * Добавляет идентификаторы дочерних (связанных по reference field или parent) объектов
         * и сохраняет нативный порядок следования Id.
         * составной ключ - группирует список id дочерних объектов по заданной ключевой фразе,
         * например, формат DomainObjectTypeName:field -> Employee:Department
         * первым элементом ключа (неявным параметром) есть название типа доменного объекта
         * разделитель между ключевыми словами ':'
         * @param ids идентификаторы дочерних объектов
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
         * @see #addChildDocumentObjectIds
         * @param id идентификатор дочернего объектов
         * @param key ключевая фраза
         */
        private void addChildDocumentObjectId(Id id, String ... key) {
            addChildDocumentObjectIds(Arrays.asList(id), key);
        }

        /**
         * Удаляет Id дочернего элемента из списков всех ключевых фраз
         * @param id удаляемого дочернего элемента
         */
        private void delChildDocumentObjectId(Id id) {
            for (Set<Id> ids : childDomainObjectIdMap.values()) {
                ids.remove(id);
            }
        }

        /**
         * Возвращает список Id для указанной ключевой фразы
         * @param key - ключевая фраза
         * @return возвращает список id дочерних (связанных, через внешние ключи) доменных объектов,
         * null - если не существует списка для указанной ключевой фразы
         */
        private List<Id> getChildDomainObjectIds(String... key) {
            String complexKey = generateKey(key);
            return childDomainObjectIdMap.containsKey(complexKey)
                    ? new ArrayList(childDomainObjectIdMap.get(complexKey)) : null;
        }

        /**
         * Возвращает список Id родительских доменных объектов
         * @return список Id родительских доменных объектов
         */
        private Set<Id> getParentDomainObjectIdSet() {
            return parentDomainObjectIdSet;
        }

        /**
         * @return клон доменного объекта
         */
        private DomainObject getDomainObject() {
            return domainObject;
        }

        /**
         * Создает составной ключ (ключевая фраза)
         * разделитель между ключевыми словами ':'
         * @param key набор ключевых слов
         * @return составной ключ (ключевая фраза)
         */

        static String generateKey(String ... key) {
            if (key.length == 1) {
                return key[0];
            }
            int capacity = 0;
            for (String k : key) {
                capacity += k.length() + 1;
            }
            StringBuilder sb = new StringBuilder(capacity);
            for (String k : key) {
                if (sb.length() > 0) {
                    sb.append(":");
                }
                sb.append(k);
            }
            return sb.toString();
        }

        /**
         * Устанавливает в null - domainObject
         * Очищает список дочерних элементов и ключевых фраз
         */
        private void clear() {
            domainObject = null;
            childDomainObjectIdMap.clear();
        }
    }

    /**
     * Возвращает узел DomainObjectNode, если для заданного Id
     * не существует DomainObjectNode, тогда создает DomainObjectNode
     * и добавляет его в транзакционный кеш
     * @param id запрашиваемого доменного объекта
     * @return контейнер (узел) доменного объекта
     */
    private DomainObjectNode createDomainObjectNode(Id id) {
        DomainObjectNode don = (DomainObjectNode) getTxReg().getResource(id);
        if (don == null) {
            don = new DomainObjectNode();
            getTxReg().putResource(id, don);
        }
        return don;
    }

    /**
     * Проверяет наличие узла DomainObjectNode для заданного Id
     * @param id запрашиваемого доменного объекта
     * @return true - если существует контейнер с указанным id, иначе false
     */
    private boolean isEmptyDomainObjectNode(Id id) {
        return getTxReg().getResource(id) == null;
    }

    /**
     * Возвращает список Id родительских доменных объектов,
     * из reference field и parent field.
     * @param dobj доменный объект
     * @return список Id родительских доменных объектов
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
     * Кеширование DomainObject, в транзакционный кеш добавляется клон DomainObject
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    public Id putObjectToCache(DomainObject dobj) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        DomainObjectNode don = createDomainObjectNode(dobj.getId());
        don.setDomainObject(dobj);
        Map<String, Id> newIdParentMap = getRefIdAndFieldMap(dobj);
        Set<Id> prevIdParentSet = don.getParentDomainObjectIdSet();
        Set<Id> newIdParentSet = new HashSet<>();
        for (Map.Entry<String, Id> ent : newIdParentMap.entrySet()) {
            newIdParentSet.add(ent.getValue());
            if (!prevIdParentSet.contains(ent.getValue())) {
                DomainObjectNode parentDon = createDomainObjectNode(ent.getValue());
                parentDon.addChildDocumentObjectId(dobj.getId(), dobj.getTypeName(), ent.getKey());
            }
        }
        for (Id id : prevIdParentSet) {
            if (!newIdParentSet.contains(id) && !isEmptyDomainObjectNode(id)) {
                DomainObjectNode parentDon = createDomainObjectNode(id);
                parentDon.delChildDocumentObjectId(id);
            }
        }
        don.getParentDomainObjectIdSet().clear();
        don.getParentDomainObjectIdSet().addAll(newIdParentSet);
        return dobj.getId();
    }

    /**
     * Кеширование списка DomainObject,
     * @see #putObjectToCache(DomainObject)
     * @param dobjs список кешируемых объектов
     * @return список Id кешируемых объектов
     */
    public List<Id> putObjectToCache(List<DomainObject> dobjs) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        List<Id> ids = new ArrayList<>();
        for (DomainObject dobj : dobjs) {
            ids.add(putObjectToCache(dobj));
        }
        return ids;
    }

    /**
     * Кеширование списка DomainObject, для указанного родителя и ключевой фразы
     * @param parentId - родительский Id
     * @param dobjs список кешируемых объектов
     * @param key ключевая фраза
     * @return список Id доменных объектов добавленных в кеш
     */
    public List<Id> putObjectToCache(Id parentId, List<DomainObject> dobjs, String ... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        List<Id> ids = putObjectToCache(dobjs);
        createDomainObjectNode(parentId).addChildDocumentObjectIds(ids, key);
        return ids;
    }

    /**
     * Возвращает клон доменного объекта из кеш
     * @param id - Id запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    public DomainObject getObjectToCache(Id id) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        return isEmptyDomainObjectNode(id) ? null : createDomainObjectNode(id).getDomainObject();
    }

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param ids - список Id запрашиваемых доменных объектов
     * @return список доменных объектов, null - если не согласованно с базой данных
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
     * Возвращает список клонированных доменных объектов из кеш
     * @param parentId - родительский Id
     * @param key ключевая фраза
     * @return список доменных объектов, null - если не согласованно с базой данных
     */
    public List<DomainObject> getObjectToCache(Id parentId, String ... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        final String complexKey = DomainObjectNode.generateKey(key);
        if (isEmptyDomainObjectNode(parentId)) {
            return null;
        }
        DomainObjectNode parentDon = createDomainObjectNode(parentId);
        List<Id> ids = parentDon.getChildDomainObjectIds(complexKey);
        if (ids == null) {
            return null;
        }

        List<DomainObject> ret = new ArrayList<>();
        for (Id id : ids) {
            DomainObjectNode don = isEmptyDomainObjectNode(id) ? null : createDomainObjectNode(id);
            if (don != null && don.getDomainObject() != null) {
                ret.add(don.getDomainObject());
            }
        }
        return ret.size() == 0 ? null : ret;
    }

    /**
     * Удаляет доменный объет из транзакционного кеша
     * @param id - доменного объекта
     */
    public void removeObjectFromCache(Id id) {
        if (!isEmptyDomainObjectNode(id)) {
            DomainObject dobj = isEmptyDomainObjectNode(id) ? null : createDomainObjectNode(id).getDomainObject();
            if (dobj != null) {
                Map<String, Id> idMap = getRefIdAndFieldMap(dobj);
                for (Map.Entry<String, Id> ent : idMap.entrySet()) {
                    DomainObjectNode parentDon = isEmptyDomainObjectNode(ent.getValue())
                            ? null : createDomainObjectNode(ent.getValue());
                    if (parentDon != null) {
                        parentDon.delChildDocumentObjectId(id);
                    }
                }
            }
            createDomainObjectNode(id).clear();
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