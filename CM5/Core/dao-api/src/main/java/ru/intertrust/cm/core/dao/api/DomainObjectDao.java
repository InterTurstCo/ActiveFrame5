package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;

import java.util.List;

/**
 * DAO для работы с доменными объектами выполняет операции создания, модификации,
 * удаления, поиска и т.д.
 *
 */
public interface DomainObjectDao {


    /**
     * Создает новый доменный объект
     * @param domainObject
     *            доменный объект который будет создан
     * @return созданыый доменный объект
     */
    public DomainObject create(DomainObject domainObject);

    /**
     * Модифицирует переданный доменный объект
     * @param domainObject
     *            доменный объект который надо изменить
     * @return возвращет модифицированный доменный объект
     * @throws InvalidIdException
     *             если идентификатор доменный объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     * @throws OptimisticLockException
     *             если объект уже был модифицирован другим пользователем
     */
    public DomainObject update(DomainObject domainObject)
            throws InvalidIdException, ObjectNotFoundException, OptimisticLockException;

    /**
     * Удаляет доменный объект по уникальному идентифткатору
     * @param id
     *            уникальный идентификатор объекта который надо удалить
     * @throws InvalidIdException
     *             если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     */
    public void delete(Id id) throws InvalidIdException, ObjectNotFoundException;

    /**
     * Проверяет существует ли доменный объект с переданным уникальным
     * идентификатором
     * @param id
     *            идентификатор доменного объекта
     * @throws InvalidIdException
     *             если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     * @return true если объект существует иначе возвращает false
     */
    public boolean exists(Id id) throws InvalidIdException;

    /**
     * Поиск коллекции доменных объектов, используя фильтры и сортировку
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции.
     * @param filterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     * @return
     */
    IdentifiableObjectCollection findCollection(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues, SortOrder sortOrder, int offset, int limit);

    /**
     * Поиск количества записей в коллекции доменных объектов используя фильтры
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции
     * @return
     */
    int findCollectionCount(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues);

    /**
     * Поиск доменного объекта по уникальному идентификатору в системе.
     * @param id идентификатору доменного объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    DomainObject find(Id id);

    /**
     * Поиск списка доменных объектов по уникальным идентификаторам в системе.
     * @param ids уникальные идентификаторы
     * @return {@link List< ru.intertrust.cm.core.business.api.dto.DomainObject >}
     */
    List<DomainObject> find(List<Id> ids);
}
