package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.exception.DaoException;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.*;

/**
 * Кэш уровня транзакции для DomainObject.
 * Сервис предоставляет кеширование DomainObject в транзакционном кеше.
 * Кеш формирует иерархическую структуру зависимостей в виде дерева, между внутренними узлами кеширования.
 * Узел кеширования - контейнер, который хранит в себе информацию о доменном объекте
 * и его связях с другими узлами кеша.
 * Зависимости между узлами кеша выстраиваются по ключевой фразе.
 * Ключевая фраза - имя типа дочернего доменного объекта и его сыллочного поля на родительский доменный объект
 * или уникальный список ключевых слов,
 * является связующим звеном между родительскими и дочерними узлами кеша,
 * т.е. формирует дерево зависимостей между узлами кеша,
 * где один и тот же родительский узел может владеть несколькими списками дочерних узлов,
 * объедененных (различаемых) ключевой фразой. Имеет формат в виде строки в двух вариантах:
 * 1) [имя типа доменного объекта]:[ссылочное поле доменного объекта],
 * где [имя типа доменного объекта]:[ссылочное поле доменного объекта] - задается неявным образом сервисом,
 * если список ключевых слов не используется.
 * 2) [ключ 1]:...:[ключ n], где [ключ n] - список ключевых слов, метки-идентификаторы,
 * задают ссылочную уникальность ключевой фразы.
 */
@Service
public class DomainObjectCacheServiceImpl {

    /**
     * Псевдо-идентификатор для кэширования списков доменных объектов, не имеющих родителя
     */
    private static final Id GLOBAL_PSEUDO_ID = new RdbmsId(-1, -1);

    @Resource
    private TransactionSynchronizationRegistry txReg;

    static private class DomainObjectNode {
        private Map<String, LinkedHashSet<Id>> childDomainObjectIdMap = new HashMap<>();
        private Set<Id> parentDomainObjectIdSet = new HashSet<>();
        //clone доменного объекта
        private DomainObject domainObject;

        private DomainObjectNode() {
        }

        private void setDomainObject(DomainObject domainObject) {
            //deep clone
            this.domainObject = domainObject == null
                    ? null
                    : (DomainObject) SerializationUtils.deserialize(SerializationUtils.serialize(domainObject));
        }

        private void addChildDoNodeIds(List<Id> ids, String... key) {
            //key - список ключевых слов, см. выше
            String complexKey = generateKey(key);
            LinkedHashSet idSet = childDomainObjectIdMap.get(complexKey);
            if (idSet == null) {
                idSet = new LinkedHashSet();
                childDomainObjectIdMap.put(complexKey, idSet);
            }
            idSet.addAll(ids);
        }


        private void addChildDoNodeId(Id id, String... key) {
            addChildDoNodeIds(Arrays.asList(id), key);
        }

        private void delChildDoNodeId(Id id) {
            for (Set<Id> ids : childDomainObjectIdMap.values()) {
                ids.remove(id);
            }
        }

        private List<Id> getChildDoNodeIds(String... key) {
            String complexKey = generateKey(key);
            return childDomainObjectIdMap.containsKey(complexKey)
                    ? new ArrayList(childDomainObjectIdMap.get(complexKey)) : null;
        }

        private Set<Id> getParentDoNodeIdSet() {
            return parentDomainObjectIdSet;
        }

        private DomainObject getDomainObject() {
            return domainObject == null
                    ? null
                    : (DomainObject) SerializationUtils.deserialize(SerializationUtils.serialize(domainObject));
        }

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

        private void clear() {
            domainObject = null;
            childDomainObjectIdMap.clear();
        }
    }

    private DomainObjectNode createDomainObjectNode(Id id) {
        //Возвращает узел DomainObjectNode, если для заданного Id
        //не существует DomainObjectNode, тогда создает DomainObjectNode
        //и добавляет его в транзакционный кеш
        DomainObjectNode don = (DomainObjectNode) getTxReg().getResource(id);
        if (don == null) {
            don = new DomainObjectNode();
            getTxReg().putResource(id, don);
        }
        return don;
    }

    private boolean isEmptyDomainObjectNode(Id id) {
        return getTxReg().getResource(id) == null;
    }

    private Map<String, Id> getRefIdAndFieldMap(DomainObject dobj) {
        //Возвращает карту зависимостей [ссылочное поле]-[иденификатор доменного объекта],
        //где поле - название ссылочного поля в доменном объекте, см. структуру для DomainObject
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
     * Кеширование DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
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
        Set<Id> prevIdParentSet = don.getParentDoNodeIdSet();
        Set<Id> newIdParentSet = new HashSet<>();
        for (Map.Entry<String, Id> ent : newIdParentMap.entrySet()) {
            newIdParentSet.add(ent.getValue());
            if (!prevIdParentSet.contains(ent.getValue())) {
                DomainObjectNode parentDon = createDomainObjectNode(ent.getValue());
                parentDon.addChildDoNodeId(dobj.getId(), dobj.getTypeName(), ent.getKey());
            }
        }
        for (Id id : prevIdParentSet) {
            if (!newIdParentSet.contains(id) && !isEmptyDomainObjectNode(id)) {
                DomainObjectNode parentDon = createDomainObjectNode(id);
                parentDon.delChildDoNodeId(id);
            }
        }
        don.getParentDoNodeIdSet().clear();
        don.getParentDoNodeIdSet().addAll(newIdParentSet);
        return dobj.getId();
    }

    /**
     * Кеширование списка DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject)
     * @param dobjs список кешируемых доменных объектов
     * @return список идентификаторов кешируемых доменных объектов
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
     * Кеширование списка DomainObject, в транзакционный кеш,
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject)
     * @param parentId - идентификатор родительского доменного объекта.
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    public List<Id> putObjectToCache(Id parentId, List<DomainObject> dobjs, String ... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        List<Id> ids = putObjectToCache(dobjs);
        createDomainObjectNode(parentId).addChildDoNodeIds(ids, key);
        return ids;
    }

    /**
     * Кеширование списка DomainObject в транзакционный кеш для случая, когда список не имеет родительского доменного
     * объекта.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject)
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    public List<Id> putObjectToCache(List<DomainObject> dobjs, String ... key) {
        return putObjectToCache(GLOBAL_PSEUDO_ID, dobjs, key);
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
     * @param parentId - - идентификатор родительского доменного объекта,
     * к которому привязан список дочерних доменных объектов, через ключевую фразу
     * @param key ключевая фраза,  см. определение в описании класса.
     * @return список дочерних доменных объектов по отношению к parentId.
     * @throws DaoException - если key == null или содержит пустой список.
     * null - если не согласованно с базой данных
     */
    public List<DomainObject> getObjectToCache(Id parentId, String ... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        final String complexKey = DomainObjectNode.generateKey(key);
        if (isEmptyDomainObjectNode(parentId)) {
            return null;
        }
        DomainObjectNode parentDon = createDomainObjectNode(parentId);
        List<Id> ids = parentDon.getChildDoNodeIds(complexKey);
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
     * Возвращает список клонированных доменных объектов из кеш в случае, когда список не имеет родительского
     * доменного объекта
     * @param key ключевая фраза,  см. определение в описании класса.
     * @return список дочерних доменных объектов по отношению к parentId.
     * @throws DaoException - если key == null или содержит пустой список.
     * null - если не согласованно с базой данных
     */
    public List<DomainObject> getObjectToCache(String ... key) {
        return getObjectToCache(GLOBAL_PSEUDO_ID, key);
    }

    /**
     * Удаляет доменный объект из транзакционного кеша
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
                        parentDon.delChildDoNodeId(id);
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