package ru.intertrust.cm.core.dao.api;

import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.exception.DaoException;

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
public interface DomainObjectCacheService {

    /**
     * Категории кеширования коллекций. Категории нужны для формирования уникального ключа в кеше. Так одному ДО могут
     * сопоставляться разные коллекции, уникально определяемые категорией в кеше.
     * @author atsvetkov
     */
    public enum COLLECTION_CACHE_CATEGORY {
        GROUP_FOR_PERSON,
        PERSON_IN_GROUP,
        PERSON_IN_GROUP_AND_SUBGROUP,
        CHILD_GROUPS,
        ALL_CHILD_GROUPS,
        ALL_PARENT_GROUPS
    }

    /**
     * Кеширование DomainObject после чтения (не требуется обновление связанных объектов) в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    public Id putOnRead(DomainObject dobj, AccessToken accessToken);

    /**
     * Кеширование DomainObject после чтения (не требуется обновление связанных объектов)
     * в транзакционный кеш без ограничения по правам доступа.
     * Используется для кэширования DomainObject, доступных всем типам пользователей, например, Status, Person.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    Id putOnRead(DomainObject dobj);

    /**
     * Кеширование DomainObject после сохранения/изменения (требуется обновление связанных объектов) в транзакционный кеш.
     * Используется для кэширования DomainObject, доступных всем типам пользователей, например, Status, Person.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    Id putOnUpdate(DomainObject dobj, AccessToken accessToken);

    /**
     * Кеширование списка DomainObject после чтения (не требуется обновление связанных объектов) в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putOnRead(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @return список идентификаторов кешируемых доменных объектов
     */
    public List<Id> putAllOnRead(List<DomainObject> dobjs, AccessToken accessToken);

    /**
     * Кеширование списка DomainObject после чтения (не требуется обновление связанных объектов) в транзакционный кеш,
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putOnRead(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param parentId - идентификатор родительского доменного объекта.
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    public void putAllOnRead(Id parentId, List<DomainObject> dobjs, AccessToken accessToken, String... key);

    /**
     * Кеширование списка DomainObject после чтения (не требуется обновление связанных объектов) в транзакционный кеш
     * для случая, когда список не имеет родительского доменного объекта.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putOnRead(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @param key ключевая фраза - формирует уникальный список дочерних доменных объектов
     * для указанного родительского доменного объекта.
     * @return список идентификаторов доменных объектов добавленных в кеш
     * @throws DaoException - если key == null или содержит пустой список.
     */
    public void putAllOnRead(List<DomainObject> dobjs, AccessToken accessToken, String... key);

    /**
     * Кеширование коллекции объектов после чтения (не требуется обновление связанных объектов) в транзакционном кеше.
     * Кеш сохраняет в своей внутренней структуре клон передаваемой коллекции.
     * Коллекции кешируются по Id родительского объекта и категории тестирования, т.к. с одним
     * Id могут быть связаны разные коллекции (с разными категориями).
     * @param parentId Id родительского объекта
     * @param dobjs кешируемая коллекция
     * @param key категория тестирования
     * @return
     */
    public void putCollectionOnRead(Id parentId, List<DomainObject> dobjs, String... key);

    /**
     * Возвращает клонированную коллекцию доменных объектов из кеша.
     * @param parentId Id родительского объекта
     * @param key категория тестирования
     * @return
     */
    public List<DomainObject> getCollection(Id parentId, String... key);

    /**
     * Очищает коллекционный кеш по категории.
     * @param key
     */
    public void clearCollection(String... key);

    /**
     * Возвращает клон доменного объекта из кеш
     * @param id - Id запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    public DomainObject get(Id id, AccessToken accessToken);

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param ids - список Id запрашиваемых доменных объектов
     * @return список доменных объектов, null - если не согласованно с базой данных
     */
    public List<DomainObject> getAll(List<? extends Id> ids, AccessToken accessToken);

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param parentId - - идентификатор родительского доменного объекта,
     * к которому привязан список дочерних доменных объектов, через ключевую фразу
     * @param key ключевая фраза,  см. определение в описании класса.
     * @return список дочерних доменных объектов по отношению к parentId.
     * @throws DaoException - если key == null или содержит пустой список.
     * null - если не согласованно с базой данных
     */
    public List<DomainObject> getAll(Id parentId, AccessToken accessToken, String... key);

    /**
     * Возвращает список клонированных доменных объектов из кеш в случае, когда список не имеет родительского
     * доменного объекта
     * @param key ключевая фраза,  см. определение в описании класса.
     * @return список дочерних доменных объектов по отношению к parentId.
     * @throws DaoException - если key == null или содержит пустой список.
     * null - если не согласованно с базой данных
     */
    public List<DomainObject> getAll(AccessToken accessToken, String... key);

    /**
     * Возвращает клон доменного объекта из кеш
     * @param uniqueKey - уникальный ключ запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    DomainObject get(String domainObjectType, Map<String, Value> uniqueKey, AccessToken accessToken);

    /**
     * Удаляет доменный объект из транзакционного кеша
     * @param id - доменного объекта
     */
    public void evict(Id id);

    /**
     * Очищает кэш
     */
    void clear();
}
