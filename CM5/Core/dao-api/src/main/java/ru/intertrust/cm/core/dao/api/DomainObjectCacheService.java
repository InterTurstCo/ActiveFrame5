package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
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
     * Кеширование DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    public Id putObjectToCache(DomainObject dobj, AccessToken accessToken);

    /**
     * Кеширование DomainObject, в транзакционный кеш без ограничения по правам доступа.
     * Используется для кэширования DomainObject, доступных всем типам пользователей, например, Status, Person.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @param dobj кешируемый объект
     * @return Id кешируемого объекта
     */
    Id putObjectToCache(DomainObject dobj);

    /**
     * Кеширование списка DomainObject, в транзакционный кеш.
     * Кеш сохраняет в своей внутренней структуре клон передаваемого DomainObject.
     * @see #putObjectToCache(DomainObject, ru.intertrust.cm.core.dao.access.AccessToken)
     * @param dobjs список кешируемых доменных объектов
     * @return список идентификаторов кешируемых доменных объектов
     */
    public List<Id> putObjectsToCache(List<DomainObject> dobjs, AccessToken accessToken);
    
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
    public List<Id> putObjectsToCache(Id parentId, List<DomainObject> dobjs, AccessToken accessToken, String... key);
    
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
    public List<Id> putObjectsToCache(List<DomainObject> dobjs, AccessToken accessToken, String... key);

    /**
     * Возвращает клон доменного объекта из кеш
     * @param id - Id запрашиваемого доменного объекта
     * @return клон доменного объект
     */
    public DomainObject getObjectFromCache(Id id, AccessToken accessToken);

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param ids - список Id запрашиваемых доменных объектов
     * @return список доменных объектов, null - если не согласованно с базой данных
     */
    public List<DomainObject> getObjectsFromCache(List<? extends Id> ids, AccessToken accessToken);

    /**
     * Возвращает список клонированных доменных объектов из кеш
     * @param parentId - - идентификатор родительского доменного объекта,
     * к которому привязан список дочерних доменных объектов, через ключевую фразу
     * @param key ключевая фраза,  см. определение в описании класса.
     * @return список дочерних доменных объектов по отношению к parentId.
     * @throws DaoException - если key == null или содержит пустой список.
     * null - если не согласованно с базой данных
     */
    public List<DomainObject> getObjectsFromCache(Id parentId, AccessToken accessToken, String... key);

    /**
     * Возвращает список клонированных доменных объектов из кеш в случае, когда список не имеет родительского
     * доменного объекта
     * @param key ключевая фраза,  см. определение в описании класса.
     * @return список дочерних доменных объектов по отношению к parentId.
     * @throws DaoException - если key == null или содержит пустой список.
     * null - если не согласованно с базой данных
     */
    public List<DomainObject> getObjectsFromCache(AccessToken accessToken, String... key);
    
    /**
     * Удаляет доменный объект из транзакционного кеша
     * @param id - доменного объекта
     */
    public void removeObjectFromCache(Id id);

    /**
     * Очищает кэш
     */
    void clear();
}
