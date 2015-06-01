package ru.intertrust.cm.core.dao.impl;

import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.util.ObjectCloner;

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
public class DomainObjectCacheServiceImpl implements DomainObjectCacheService {

    private static final String LIMITATION_TYPE_MAP_KEY = "AccessLimitationTypeCacheMap";

    private static final String OBJECT_COLLECTION_MAP_KEY = "ObjectsCollectionCacheMap";

    /**
     * позволяет отключить кэш
     */
    @org.springframework.beans.factory.annotation.Value("${cache.domainObject.enabled:true}")
    private Boolean cacheEnabled;

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
            this.domainObject = ObjectCloner.getInstance().cloneObject(domainObject);
        }
        

        private void setChildDoNodeIds(List<Id> ids, String... key) {
            //key - список ключевых слов, см. выше
            String complexKey = generateKey(key);
            LinkedHashSet<Id> idSet = childDomainObjectIdMap.get(complexKey);

            if (idSet == null) {
                idSet = new LinkedHashSet<>();
                childDomainObjectIdMap.put(complexKey, idSet);
            } else {
                idSet.clear();
            }

            idSet.addAll(ids);
        }

        private void addChildDoNodeIds(List<Id> ids, String... key) {
            //key - список ключевых слов, см. выше
            String complexKey = generateKey(key);
            LinkedHashSet<Id> idSet = childDomainObjectIdMap.get(complexKey);
            if (idSet == null) {
                return;
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
                    ? new ArrayList<>(childDomainObjectIdMap.get(complexKey)) : null;
        }

        private Set<Id> getParentDoNodeIdSet() {
            return parentDomainObjectIdSet;
        }

        private DomainObject getDomainObject() {
            return domainObject == null ? null : ObjectCloner.getInstance().cloneObject(domainObject);
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
            parentDomainObjectIdSet.clear();
        }

        private DomainObject getInternalDomainObject() {
            return domainObject;
        }

        private void setInternalDomainObject(DomainObject domainObject) {
            this.domainObject = domainObject;
        }
    }

    /**
     * Кеширование DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    @Override
    public Id putObjectToCache(DomainObject dobj, AccessToken accessToken) {
        return putObjectToCache(dobj, accessToken.getAccessLimitationType());
    }

    @Override
    public Id putObjectToCache(DomainObject dobj) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        Id id = null;
        for (AccessToken.AccessLimitationType accessLimitationType : getAccessLimitationMap().keySet()) {
            id = putObjectToCache(dobj, accessLimitationType);
        }

        return id;
    }


    /**
     * Кеширование списка DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @return список идентификаторов кешируемых доменных объектов
     */
    @Override
    public List<Id> putObjectsToCache(List<DomainObject> dobjs, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null || dobjs == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        List<Id> ids = new ArrayList<>();
        for (DomainObject dobj : dobjs) {
            ids.add(putObjectToCache(dobj, accessToken));
        }
        return ids;
    }

    /**
     * Кеширование списка DomainObject, в транзакционный кеш,
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param parentId - идентификатор родительского доменного объекта.
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    @Override
    public List<Id> putObjectsToCache(Id parentId, List<DomainObject> dobjs, AccessToken accessToken, String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        List<Id> ids = putObjectsToCache(dobjs, accessToken);
        getOrCreateDomainObjectNode(parentId, accessToken.getAccessLimitationType()).setChildDoNodeIds(ids, key);
        return ids;
    }

    /**
     * Кеширование списка DomainObject в транзакционный кеш для случая, когда список не имеет родительского доменного
     * объекта.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    @Override
    public List<Id> putObjectsToCache(List<DomainObject> dobjs, AccessToken accessToken, String... key) {

        if (!isCacheEnabled()) return null;

        return putObjectsToCache(GLOBAL_PSEUDO_ID, dobjs, accessToken, key);
    }
    
    @Override
    public List<Id> putObjectCollectionToCache(Id parentId, List<DomainObject> dobjs, String... key) {
        if (getTxReg().getTransactionKey() == null || dobjs == null) {
            return null;
        }

        if (!isCacheEnabled()) {
            return null;
        }

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }

        Map<String, List<DomainObject>> objectCollectionMap = getObjectCollectionMap();

        String objectCollectionKey = generateObjectCollectionKey(parentId, key);

        List<DomainObject> domainObjects = objectCollectionMap.get(objectCollectionKey);

        if (domainObjects == null) {
            domainObjects = new ArrayList<>();
            getObjectCollectionMap().put(objectCollectionKey, domainObjects);
        } else {
            domainObjects.clear();
        }

        List<Id> ids = new ArrayList<Id>();
        for (DomainObject object : dobjs) {
            DomainObject clonedObject = ObjectCloner.getInstance().cloneObject(object);
            ids.add(clonedObject.getId());
            domainObjects.add(clonedObject);
        }

        // TODO Do we need to put objects to cache individually?

        return ids;
    }

    private String generateObjectCollectionKey(Id parentId, String... key) {
        return DomainObjectNode.generateKey(key) + parentId.toStringRepresentation();
    }

    @Override
    public void clearObjectCollectionByKey(String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        
        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        Map<String, List<DomainObject>> objectCollectionMap = getObjectCollectionMap();
        Iterator<String> keyIterator = objectCollectionMap.keySet().iterator();
        
        while (keyIterator.hasNext()) {
            String collectionKey = keyIterator.next();
            if (collectionKey != null && collectionKey.startsWith(DomainObjectNode.generateKey(key))) {
                keyIterator.remove();
            }

        }
    }
    
    @Override
    public List<DomainObject> getObjectCollectionFromCache(Id parentId, String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) {
            return null;
        }

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        Map<String, List<DomainObject>> objectCollectionMap = getObjectCollectionMap();

        String objectCollectionKey = generateObjectCollectionKey(parentId, key);

        List<DomainObject> domainObjects = objectCollectionMap.get(objectCollectionKey);
        if (domainObjects == null) {
            return null;
        }

        return ObjectCloner.getInstance().cloneObject(domainObjects);
    }

    /**
     * Возвращает клон доменного объекта из кеш
     * @param id - Id запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    @Override
    public DomainObject getObjectFromCache(Id id, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        return isEmptyDomainObjectNode(id, accessToken.getAccessLimitationType()) ? null :
                getOrCreateDomainObjectNode(id, accessToken.getAccessLimitationType()).getDomainObject();
    }

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param ids - список Id запрашиваемых доменных объектов
     * @return список доменных объектов, null - если не согласованно с базой данных
     */
    @Override
    public List<DomainObject> getObjectsFromCache(List<? extends Id> ids, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        List<DomainObject> dobjs = new ArrayList<>();
        for (Id id : ids) {
            DomainObject dobj = getObjectFromCache(id, accessToken);
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
    @Override
    public List<DomainObject> getObjectsFromCache(Id parentId, AccessToken accessToken, String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        final String complexKey = DomainObjectNode.generateKey(key);
        if (isEmptyDomainObjectNode(parentId, accessToken.getAccessLimitationType())) {
            return null;
        }
        DomainObjectNode parentDon = getOrCreateDomainObjectNode(parentId, accessToken.getAccessLimitationType());
        List<Id> ids = parentDon.getChildDoNodeIds(complexKey);
        if (ids == null) {
            return null;
        }

        List<DomainObject> ret = new ArrayList<>();
        for (Id id : ids) {
            DomainObjectNode don = isEmptyDomainObjectNode(id, accessToken.getAccessLimitationType()) ? null :
                    getOrCreateDomainObjectNode(id, accessToken.getAccessLimitationType());
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
    @Override
    public List<DomainObject> getObjectsFromCache(AccessToken accessToken, String... key) {

        if (!isCacheEnabled()) return null;

        return getObjectsFromCache(GLOBAL_PSEUDO_ID, accessToken, key);
    }

    /**
     * Удаляет доменный объект из транзакционного кеша
     * @param id - доменного объекта
     */
    @Override
    public void removeObjectFromCache(Id id) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }

        if (!isCacheEnabled()) return;

        for (AccessToken.AccessLimitationType limitationType : getAccessLimitationMap().keySet()) {
            if (!isEmptyDomainObjectNode(id, limitationType)) {
                DomainObject dobj = isEmptyDomainObjectNode(id, limitationType) ? null : getOrCreateDomainObjectNode(id, limitationType).getDomainObject();
                if (dobj != null) {
                    Map<String, Id> idMap = getRefIdAndFieldMap(dobj);
                    for (Map.Entry<String, Id> ent : idMap.entrySet()) {
                        DomainObjectNode parentDon = isEmptyDomainObjectNode(ent.getValue(), limitationType)
                                ? null : getOrCreateDomainObjectNode(ent.getValue(), limitationType);
                        if (parentDon != null) {
                            parentDon.delChildDoNodeId(id);
                        }
                    }
                }
                getOrCreateDomainObjectNode(id, limitationType).clear();
            }
        }
    }

    public void removeChildNodesByKey(Id id, String... rey){
        
    }
    
    @Override
    public void clear() {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }

        getAccessLimitationMap().clear();
    }

    private Id putObjectToCache(DomainObject dobj, AccessToken.AccessLimitationType accessLimitationType) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        DomainObjectNode dobjNode = getOrCreateDomainObjectNode(dobj.getId(), accessLimitationType);
        dobjNode.setDomainObject(dobj);
        updateDomainObjectInAllCaches(dobjNode); // Необходимо обновить доменный объект во всех кэшах одним и тем же объектом

        for (Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> cacheEntry : getAccessLimitationMap().entrySet()) {
            if (cacheEntry.getKey().equals(accessLimitationType) ||
                    cacheEntry.getKey().equals(AccessToken.AccessLimitationType.UNLIMITED)) {
                updateLinkedDoCaches(dobj, cacheEntry);
            } else {
                clearLinkedDoCaches(dobj, cacheEntry);
            }
        }

        return dobj.getId();
    }

    private DomainObjectNode getOrCreateDomainObjectNode(Id id, AccessToken.AccessLimitationType limitationType) {
        //Возвращает узел DomainObjectNode, если для заданного Id
        //не существует DomainObjectNode, тогда создает DomainObjectNode
        //и добавляет его в транзакционный кеш
        Map<String, DomainObjectNode> idMap = getAccessLimitationMap().get(limitationType);
        if (idMap == null) {
            idMap = new HashMap<>();
            getAccessLimitationMap().put(limitationType, idMap);
        }

        String key = DomainObjectNode.generateKey(id.toStringRepresentation());
        DomainObjectNode don = idMap.get(key);
        if (don == null) {
            don = new DomainObjectNode();
            idMap.put(key, don);
        }

        return don;
    }

    // Делает клон доменного объекта и обновляет этим клоном доменный объект во всех кэшах
    private void updateDomainObjectInAllCaches(DomainObjectNode domainObjectNode) {
        DomainObject domainObject = domainObjectNode.getInternalDomainObject();
        String key = DomainObjectNode.generateKey(domainObject.getId().toStringRepresentation());

        for (Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> entry : getAccessLimitationMap().entrySet()) {
            DomainObjectNode don = entry.getValue().get(key);
            if (don != null && don.getInternalDomainObject() != null) {
                don.setInternalDomainObject(domainObject);
            }
        }
    }

    private void clearLinkedDoCaches(DomainObject dobj, Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> cacheEntry) {
        DomainObjectNode don = getOrCreateDomainObjectNode(dobj.getId(), cacheEntry.getKey());

        Map<String, Id> newIdParentMap = getRefIdAndFieldMap(dobj);
        Set<Id> prevIdParentSet = don.getParentDoNodeIdSet();

        for (Map.Entry<String, Id> ent : newIdParentMap.entrySet()) {
            DomainObjectNode parentDon = getOrCreateDomainObjectNode(ent.getValue(), cacheEntry.getKey());
            parentDon.clear();
        }

        for (Id id : prevIdParentSet) {
            DomainObjectNode parentDon = getOrCreateDomainObjectNode(id, cacheEntry.getKey());
            parentDon.clear();
        }

        don.getParentDoNodeIdSet().clear();
    }

    private void updateLinkedDoCaches(DomainObject dobj, Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> cacheEntry) {
        DomainObjectNode don = getOrCreateDomainObjectNode(dobj.getId(), cacheEntry.getKey());

        Map<String, Id> newIdParentMap = getRefIdAndFieldMap(dobj);
        Set<Id> prevIdParentSet = don.getParentDoNodeIdSet();
        Set<Id> newIdParentSet = new HashSet<>();

        for (Map.Entry<String, Id> ent : newIdParentMap.entrySet()) {
            newIdParentSet.add(ent.getValue());
            if (!prevIdParentSet.contains(ent.getValue()) && !isEmptyDomainObjectNode(ent.getValue(), cacheEntry.getKey())) {
                DomainObjectNode parentDon = getOrCreateDomainObjectNode(ent.getValue(), cacheEntry.getKey());
                parentDon.addChildDoNodeId(dobj.getId(), dobj.getTypeName(), ent.getKey());
            }
        }

        for (Id id : prevIdParentSet) {
            if (!newIdParentSet.contains(id) && !isEmptyDomainObjectNode(id, cacheEntry.getKey())) {
                DomainObjectNode parentDon = getOrCreateDomainObjectNode(id, cacheEntry.getKey());
                parentDon.delChildDoNodeId(id);
            }
        }

        don.getParentDoNodeIdSet().clear();
        don.getParentDoNodeIdSet().addAll(newIdParentSet);
    }

    private boolean isEmptyDomainObjectNode(Id id, AccessToken.AccessLimitationType limitationType) {
        Map<String, DomainObjectNode> cacheMap = getAccessLimitationMap().get(limitationType);
        if (cacheMap == null) {
            cacheMap =  new HashMap<>();
            getAccessLimitationMap().put(limitationType, cacheMap);
        }

        return cacheMap.get(DomainObjectNode.generateKey(id.toStringRepresentation())) == null;
    }

    private Map<String, Id> getRefIdAndFieldMap(DomainObject dobj) {
        //Возвращает карту зависимостей [ссылочное поле]-[иденификатор доменного объекта],
        //где поле - название ссылочного поля в доменном объекте, см. структуру для DomainObject
        Map<String, Id> ret = new HashMap<>();
        for (String fn : dobj.getFields()) {
            Value v = dobj.getValue(fn);
            if (v != null && v.get() != null && v instanceof ReferenceValue) {
                ret.put(fn, (Id) v.get());
            }
        }
        return ret;
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

    private Map<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> getAccessLimitationMap() {
        Map<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> accessTypeMap = (Map) getTxReg().getResource(LIMITATION_TYPE_MAP_KEY);
        if (accessTypeMap == null) {
            accessTypeMap = new HashMap<>();
            getTxReg().putResource(LIMITATION_TYPE_MAP_KEY, accessTypeMap);
        }

        return accessTypeMap;
    }

    private Map<String, List<DomainObject>> getObjectCollectionMap() {
        Map<String, List<DomainObject>> objectCollectionMap = (Map) getTxReg().getResource(OBJECT_COLLECTION_MAP_KEY);
        if (objectCollectionMap == null) {
            objectCollectionMap = new HashMap<>();
            getTxReg().putResource(OBJECT_COLLECTION_MAP_KEY, objectCollectionMap);
        }

        return objectCollectionMap;
    }

    private boolean isCacheEnabled() {
        return cacheEnabled;
    }
}