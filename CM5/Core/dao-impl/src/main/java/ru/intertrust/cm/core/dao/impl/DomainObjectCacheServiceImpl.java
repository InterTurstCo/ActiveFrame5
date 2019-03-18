package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
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
public class DomainObjectCacheServiceImpl implements DomainObjectCacheService {

    private static final String LIMITATION_TYPE_MAP_KEY = "AccessLimitationTypeCacheMap";
    private static final String OBJECT_COLLECTION_MAP_KEY = "ObjectsCollectionCacheMap";
    private static final String ID_TO_UNIQUE_KEY_MAP_KEY = "IdToUniqueKeyMap";
    private static final String UNIQUE_KEY_TO_ID_MAP_KEY = "UniqueToIdKeyMap";

    /**
     * позволяет отключить кэш
     */
    @org.springframework.beans.factory.annotation.Value("${cache.domainObject.enabled:true}")
    private Boolean cacheEnabled;

    /**
     * Псевдо-идентификатор для кэширования списков доменных объектов, не имеющих родителя
     */
    private static final Id GLOBAL_PSEUDO_ID = new RdbmsId(-1, -1);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    DomainEntitiesCloner domainEntitiesCloner;

    @Resource
    private TransactionSynchronizationRegistry txReg;

    static private class DomainObjectNode {

        private Map<String, LinkedHashSet<Id>> childDomainObjectIdMap;
        private Set<Id> parentDomainObjectIdSet;
        //clone доменного объекта
        private DomainObject domainObject;

        private DomainObjectNode() {
        }

        private Map<String, LinkedHashSet<Id>> getChildDomainObjectIdMap() {
            if (childDomainObjectIdMap == null) {
                childDomainObjectIdMap = new HashMap<>();
            }
            return childDomainObjectIdMap;
        }

        private Set<Id> getParentDomainObjectIds() {
            if (parentDomainObjectIdSet == null) {
                parentDomainObjectIdSet = new HashSet<>();
            }
            return parentDomainObjectIdSet;
        }

        private void setDomainObject(DomainObject domainObject, DomainEntitiesCloner cloner) {
            //deep clone
            this.domainObject = cloner.fastCloneDomainObject(domainObject);
        }


        private void setChildNodeIds(List<Id> ids, String... key) {
            //key - список ключевых слов, см. выше
            String complexKey = generateKey(key);
            LinkedHashSet<Id> idSet = getChildDomainObjectIdMap().get(complexKey);

            if (idSet == null) {
                idSet = new LinkedHashSet<>((int) (ids.size()/0.75 + 1));
                getChildDomainObjectIdMap().put(complexKey, idSet);
            } else {
                idSet.clear();
            }

            idSet.addAll(ids);
        }

        /**
         * Очищает child node так, чтобы он был перечитан при следующем обращении
         */
        private void clearChildNode(String... key) {
            String complexKey = generateKey(key);
            getChildDomainObjectIdMap().remove(complexKey);
        }

        /**
         * Очищает все child nodes, содержащие данный id так, чтобы они были перечитаны при следующем обращении
         */
        private void clearAllChildNodesHavingId(Id id) {
            Iterator<Map.Entry<String, LinkedHashSet<Id>>> iterator = getChildDomainObjectIdMap().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, LinkedHashSet<Id>> entry = iterator.next();
                if (entry.getValue().contains(id)) {
                    iterator.remove();
                }
            }
        }

        /**
         * Обновляет child nodes добавлением id, если они уже заполнены
         */
        private void addChildNodeIdIfNotEmpty(Id id, String... key) {
            //key - список ключевых слов, см. выше
            String complexKey = generateKey(key);
            LinkedHashSet<Id> idSet = getChildDomainObjectIdMap().get(complexKey);
            if (idSet == null) {
                return;
            }
            idSet.add(id);
        }

        /**
         * Обновляет все child nodes, содержащие id, его удалением, не приводит к последующему перечитыванию этих nodes
         */
        private void removeChildNodeId(Id id) {
            for (Set<Id> ids : getChildDomainObjectIdMap().values()) {
                ids.remove(id);
            }
        }

        private List<Id> getChildNodeIds(String... key) {
            String complexKey = generateKey(key);
            return getChildDomainObjectIdMap().containsKey(complexKey)
                    ? new ArrayList<>(getChildDomainObjectIdMap().get(complexKey)) : null;
        }

        private DomainObject getDomainObject(DomainEntitiesCloner cloner) {
            return cloner.fastCloneDomainObject(domainObject);
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
            return sb.toString().toLowerCase();
        }

        private void clear() {
            domainObject = null;
            getChildDomainObjectIdMap().clear();
            getParentDomainObjectIds().clear();
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
    public Id putOnRead(DomainObject dobj, AccessToken accessToken) {
        return put(dobj, accessToken.getAccessLimitationType(), true);
    }

    /**
     * Кеширование DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    @Override
    public Id putOnUpdate(DomainObject dobj, AccessToken accessToken) {
        return put(dobj, accessToken.getAccessLimitationType(), false);
    }

    @Override
    public Id putOnRead(DomainObject dobj) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        Id id = null;
        for (AccessToken.AccessLimitationType accessLimitationType : getAccessLimitationMap().keySet()) {
            id = put(dobj, accessLimitationType, true);
        }

        return id;
    }


    /**
     * Кеширование списка DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putOnRead(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @return список идентификаторов кешируемых доменных объектов
     */
    @Override
    public List<Id> putAllOnRead(List<DomainObject> dobjs, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null || dobjs == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        List<Id> ids = new ArrayList<>(dobjs.size());
        for (DomainObject dobj : dobjs) {
            ids.add(putOnRead(dobj, accessToken));
        }
        return ids;
    }

    /**
     * Кеширование списка DomainObject, в транзакционный кеш,
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putOnRead(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param parentId - идентификатор родительского доменного объекта.
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    @Override
    public void putAllOnRead(Id parentId, List<DomainObject> dobjs, AccessToken accessToken, String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }

        if (!isCacheEnabled()) return;

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        List<Id> ids = putAllOnRead(dobjs, accessToken);
        getOrCreateDomainObjectNode(parentId, accessToken.getAccessLimitationType()).setChildNodeIds(ids, key);
    }

    /**
     * Кеширование списка DomainObject в транзакционный кеш для случая, когда список не имеет родительского доменного
     * объекта.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putOnRead(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    @Override
    public void putAllOnRead(List<DomainObject> dobjs, AccessToken accessToken, String... key) {

        if (!isCacheEnabled()) return;

        putAllOnRead(GLOBAL_PSEUDO_ID, dobjs, accessToken, key);
    }

    @Override
    public void putCollectionOnRead(Id parentId, List<DomainObject> dobjs, String... key) {
        if (getTxReg().getTransactionKey() == null || dobjs == null) {
            return;
        }

        if (!isCacheEnabled()) {
            return;
        }

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }

        Map<String, List<DomainObject>> objectCollectionMap = getCollectionMap();

        String objectCollectionKey = generateCollectionKey(parentId, key);

        List<DomainObject> domainObjects = objectCollectionMap.get(objectCollectionKey);

        if (domainObjects == null) {
            domainObjects = new ArrayList<>(dobjs.size());
            getCollectionMap().put(objectCollectionKey, domainObjects);
        } else {
            domainObjects.clear();
        }

        domainObjects.addAll(domainEntitiesCloner.fastCloneDomainObjectList(dobjs));

        // TODO Do we need to put objects to cache individually?
    }

    @Override
    public void clearCollection(String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        Map<String, List<DomainObject>> objectCollectionMap = getCollectionMap();
        Iterator<String> keyIterator = objectCollectionMap.keySet().iterator();

        while (keyIterator.hasNext()) {
            String collectionKey = keyIterator.next();
            if (collectionKey != null && collectionKey.startsWith(DomainObjectNode.generateKey(key))) {
                keyIterator.remove();
            }

        }
    }

    @Override
    public List<DomainObject> getCollection(Id parentId, String... key) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) {
            return null;
        }

        if (key == null || key.length == 0) {
            throw new DaoException("Can't find key.");
        }
        Map<String, List<DomainObject>> objectCollectionMap = getCollectionMap();

        String objectCollectionKey = generateCollectionKey(parentId, key);

        List<DomainObject> domainObjects = objectCollectionMap.get(objectCollectionKey);
        return domainEntitiesCloner.fastCloneDomainObjectList(domainObjects);
    }

    /**
     * Возвращает клон доменного объекта из кеш
     * @param id - Id запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    @Override
    public DomainObject get(Id id, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        return isEmptyDomainObjectNode(id, accessToken.getAccessLimitationType()) ? null :
                getOrCreateDomainObjectNode(id, accessToken.getAccessLimitationType()).getDomainObject(domainEntitiesCloner);
    }

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param ids - список Id запрашиваемых доменных объектов
     * @return список доменных объектов, null - если не согласованно с базой данных
     */
    @Override
    public List<DomainObject> getAll(List<? extends Id> ids, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        List<DomainObject> dobjs = new ArrayList<>(ids.size());
        for (Id id : ids) {
            DomainObject dobj = get(id, accessToken);
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
    public List<DomainObject> getAll(Id parentId, AccessToken accessToken, String... key) {
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
        List<Id> ids = parentDon.getChildNodeIds(complexKey);
        if (ids == null) {
            return null;
        }

        List<DomainObject> ret = new ArrayList<>(ids.size());
        for (Id id : ids) {
            DomainObjectNode don = isEmptyDomainObjectNode(id, accessToken.getAccessLimitationType()) ? null :
                    getOrCreateDomainObjectNode(id, accessToken.getAccessLimitationType());
            if (don != null && don.getDomainObject(domainEntitiesCloner) != null) {
                ret.add(don.getDomainObject(domainEntitiesCloner));
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
    public List<DomainObject> getAll(AccessToken accessToken, String... key) {

        if (!isCacheEnabled()) return null;

        return getAll(GLOBAL_PSEUDO_ID, accessToken, key);
    }

    /**
     * Возвращает клон доменного объекта из кеш
     * @param uniqueKey - уникальный ключ запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    @Override
    public DomainObject get(String domainObjectType, Map<String, Value> uniqueKey, AccessToken accessToken) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }
        final Id id = getUniqueKeyToIdMap(domainObjectType).get(uniqueKey);
        if (id != null) {
            return get(id, accessToken);
        } else {
            return null;
        }
    }

    /**
     * Удаляет доменный объект из транзакционного кеша
     * @param id - доменного объекта
     */
    @Override
    public void evict(Id id) {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }

        if (!isCacheEnabled()) return;

        for (AccessToken.AccessLimitationType limitationType : getAccessLimitationMap().keySet()) {
            if (!isEmptyDomainObjectNode(id, limitationType)) {
                DomainObject dobj = isEmptyDomainObjectNode(id, limitationType) ? null : getOrCreateDomainObjectNode(id, limitationType).getDomainObject(domainEntitiesCloner);
                if (dobj != null) {
                    Set<Id> idSet = generateReferenceFieldsIdMap(dobj);
                    for (Id ent : idSet) {
                        DomainObjectNode parentDon = isEmptyDomainObjectNode(ent, limitationType)
                                ? null : getOrCreateDomainObjectNode(ent, limitationType);
                        if (parentDon != null) {
                            parentDon.removeChildNodeId(id);
                        }
                    }
                }
                getOrCreateDomainObjectNode(id, limitationType).clear();
            }
        }

        Set<Map<String, Value>> uniqueKeys = getIdToUniqueKeysMap().get(id);
        Map<Map<String, Value>, Id> uniqueKeyToIdMap =  getUniqueKeyToIdMap(domainObjectTypeIdCache.getName(id));

        if (uniqueKeys != null) {
            uniqueKeyToIdMap.remove(uniqueKeys);
            uniqueKeys.clear();
        }
    }

    @Override
    public void clear() {
        if (getTxReg().getTransactionKey() == null) {
            return;
        }

        getAccessLimitationMap().clear();
        getIdToUniqueKeysMap().clear();
        getTypeToUniqueKeysMap().clear();
    }

    private String generateCollectionKey(Id parentId, String... key) {
        return DomainObjectNode.generateKey(key) + parentId.toStringRepresentation();
    }

    private Id put(DomainObject dobj, AccessToken.AccessLimitationType accessLimitationType, boolean isRead) {
        if (getTxReg().getTransactionKey() == null) {
            return null;
        }

        if (!isCacheEnabled()) return null;

        DomainObjectNode dobjNode = getOrCreateDomainObjectNode(dobj.getId(), accessLimitationType);
        dobjNode.setDomainObject(dobj, domainEntitiesCloner);
        update(dobjNode); // Необходимо обновить доменный объект во всех кэшах одним и тем же объектом

        if (isRead) {
            return dobj.getId();
        }

        for (Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> cacheEntry : getAccessLimitationMap().entrySet()) {
            updateLinks(dobj, cacheEntry);
        }

        return dobj.getId();
    }

    private DomainObjectNode getOrCreateDomainObjectNode(Id id, AccessToken.AccessLimitationType limitationType) {
        //Возвращает узел DomainObjectNode, если для заданного Id
        //не существует DomainObjectNode, тогда создает DomainObjectNode
        //и добавляет его в транзакционный кеш
        Map<String, DomainObjectNode> idMap = getAccessLimitationMap().get(limitationType);
        if (idMap == null) {
            idMap = new HashMap<>(4);
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
    private void update(DomainObjectNode domainObjectNode) {
        DomainObject domainObject = domainObjectNode.getInternalDomainObject();
        String key = DomainObjectNode.generateKey(domainObject.getId().toStringRepresentation());

        for (Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> entry : getAccessLimitationMap().entrySet()) {
            DomainObjectNode don = entry.getValue().get(key);
            if (don != null && don.getInternalDomainObject() != null) {
                don.setInternalDomainObject(domainObject);
            }
        }

        List<UniqueKeyConfig> uniqueKeyConfigs =
                configurationExplorer.getDomainObjectTypeConfig(domainObject.getTypeName()).getUniqueKeyConfigs();
        if (uniqueKeyConfigs == null || uniqueKeyConfigs.size() == 0) {
            return;
        }

        final Set<Map<String, Value>> uniqueKeys = getUniqueKeysById(domainObject.getId());
        Map<Map<String, Value>, Id> uniqueKeyToIdMap = getUniqueKeyToIdMap(domainObject.getTypeName());

        if (uniqueKeys != null) {
            uniqueKeyToIdMap.remove(uniqueKeys);
            uniqueKeys.clear();
        }

        for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigs) {
            final List<UniqueKeyFieldConfig> uniqueKeyFieldConfigs = uniqueKeyConfig.getUniqueKeyFieldConfigs();
            HashMap<String, Value> uniqueKey = new HashMap<>((int) (uniqueKeyFieldConfigs.size()/0.75 + 1));

            for (UniqueKeyFieldConfig fieldConfig : uniqueKeyFieldConfigs) {
                uniqueKey.put(fieldConfig.getName(), domainObject.getValue(fieldConfig.getName()));
            }

            uniqueKeyToIdMap.put(uniqueKey, domainObject.getId());
            uniqueKeys.add(uniqueKey);
        }
    }

    /**
     * Обновляет ссылки на родителя и очищает ссылки на дочки у родителя
     * @param dobj
     * @param cacheEntry
     */
    private void updateLinks(DomainObject dobj, Map.Entry<AccessToken.AccessLimitationType, Map<String, DomainObjectNode>> cacheEntry) {
        DomainObjectNode don = getOrCreateDomainObjectNode(dobj.getId(), cacheEntry.getKey());

        // Новый список родителей
        Set<Id> newIdParentSet = generateReferenceFieldsIdMap(dobj);
        // Список родителей у ранее сохраненного в кэше доменного объекта
        Set<Id> prevIdParentSet = don.getParentDomainObjectIds();

        // У старых родителей очищаем кэши, где в дочках есть обновляемый доменный объект
        for (Id id : prevIdParentSet) {
            DomainObjectNode parentDon = getOrCreateDomainObjectNode(id, cacheEntry.getKey());
            parentDon.clearAllChildNodesHavingId(dobj.getId());
        }
        
        // У новых родителей очищаем все кэши, это нужно делать потому что неизвестно в каком кэше (кэшах) появится обновляемый доменный объект
        for (Id ent : newIdParentSet) {
            DomainObjectNode parentDon = getOrCreateDomainObjectNode(ent, cacheEntry.getKey());
            parentDon.clear();
        }

        // Обновляем список родителей 
        don.getParentDomainObjectIds().clear();
        don.getParentDomainObjectIds().addAll(newIdParentSet);
    }

    private boolean isEmptyDomainObjectNode(Id id, AccessToken.AccessLimitationType limitationType) {
        Map<String, DomainObjectNode> cacheMap = getAccessLimitationMap().get(limitationType);
        if (cacheMap == null) {
            cacheMap = new HashMap<>(4);
            getAccessLimitationMap().put(limitationType, cacheMap);
        }

        return cacheMap.get(DomainObjectNode.generateKey(id.toStringRepresentation())) == null;
    }

    private Set<Id> generateReferenceFieldsIdMap(DomainObject dobj) {
        //Возвращает карту зависимостей [ссылочное поле]-[иденификатор доменного объекта],
        //где поле - название ссылочного поля в доменном объекте, см. структуру для DomainObject
        Set<ReferenceFieldConfig> referenceFieldConfigs = configurationExplorer.getReferenceFieldConfigs(dobj.getTypeName());
        Set<Id> ret = new HashSet<Id>((int) (referenceFieldConfigs.size()/0.75 + 1));
        for (ReferenceFieldConfig referenceFieldConfig : referenceFieldConfigs) {
            Id id = dobj.getReference(referenceFieldConfig.getName());
            if (id != null) {
                ret.add(id);
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
            accessTypeMap = new HashMap<>(4);
            getTxReg().putResource(LIMITATION_TYPE_MAP_KEY, accessTypeMap);
        }

        return accessTypeMap;
    }

    private Set<Map<String, Value>> getUniqueKeysById(Id id) {
        Map<Id, Set<Map<String, Value>>> idToUniqueKeyMap = getIdToUniqueKeysMap();
        Set<Map<String, Value>> uniqueKeys = idToUniqueKeyMap.get(id);

        if (uniqueKeys == null) {
            uniqueKeys = new HashSet<>();
            idToUniqueKeyMap.put(id, uniqueKeys);
        }

        return uniqueKeys;
    }

    private Map<Map<String, Value>, Id> getUniqueKeyToIdMap(String domainObjectType) {
        CaseInsensitiveMap<Map<Map<String, Value>, Id>> typeToUniqueKeysMap = getTypeToUniqueKeysMap();
        Map<Map<String, Value>, Id> uniqueKeyToIdMap = typeToUniqueKeysMap.get(domainObjectType);

        if (uniqueKeyToIdMap == null) {
            uniqueKeyToIdMap = new HashMap<>();
            typeToUniqueKeysMap.put(domainObjectType, uniqueKeyToIdMap);
        }

        return uniqueKeyToIdMap;
    }

    private Map<Id, Set<Map<String, Value>>> getIdToUniqueKeysMap() {
        Map<Id, Set<Map<String, Value>>> idToUniqueKeyMap = (Map) getTxReg().getResource(ID_TO_UNIQUE_KEY_MAP_KEY);
        if (idToUniqueKeyMap == null) {
            idToUniqueKeyMap = new HashMap<>();
            getTxReg().putResource(ID_TO_UNIQUE_KEY_MAP_KEY, idToUniqueKeyMap);
        }

        return idToUniqueKeyMap;
    }

    private CaseInsensitiveMap<Map<Map<String, Value>, Id>> getTypeToUniqueKeysMap() {
        CaseInsensitiveMap<Map<Map<String, Value>, Id>> uniqueKeyToIdMap = (CaseInsensitiveMap) getTxReg().getResource(UNIQUE_KEY_TO_ID_MAP_KEY);
        if (uniqueKeyToIdMap == null) {
            uniqueKeyToIdMap = new CaseInsensitiveMap<>();
            getTxReg().putResource(UNIQUE_KEY_TO_ID_MAP_KEY, uniqueKeyToIdMap);
        }

        return uniqueKeyToIdMap;
    }

    private Map<String, List<DomainObject>> getCollectionMap() {
        Map<String, List<DomainObject>> objectCollectionMap = (Map) getTxReg().getResource(OBJECT_COLLECTION_MAP_KEY);
        if (objectCollectionMap == null) {
            objectCollectionMap = new HashMap<>();
            getTxReg().putResource(OBJECT_COLLECTION_MAP_KEY, objectCollectionMap);
        }

        return objectCollectionMap;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
}