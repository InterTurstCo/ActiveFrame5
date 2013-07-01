package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;

import java.util.Collection;
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
     * Сохраняет доменный объект. Если объект не существует в системе, создаёт его и заполняет отсутствующие атрибуты
     * значениями, сгенерированными согласно правилам, определённым для данного объекта (например, будет сгенерирован и
     * заполнен идентификатор объекта). Оригинальный Java-объект измененям не подвергается, изменения отражены в
     * возвращённом объекте.
     * @param domainObject
     * @return сохраненный доменный объект
     */
    DomainObject save(DomainObject domainObject);

    /**
     * Сохраняет список доменных объектов. Если какой-то объект не существует в системе, создаёт его и заполняет
     * отсутствующие атрибуты значениями, сгенерированными согласно правилам, определённым для данного объекта
     * (например, будет сгенерирован и заполнен идентификатор объекта). Оригинальные Java-объекты измененям
     * не подвергаются, изменения отражены в возвращённых объектах.
     * @param domainObjects доменные объекты для сохранения
     * @return список сохраненных доменныъ лбъектов
     */
    List<DomainObject> save(List<DomainObject> domainObjects);

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
     * Удаляет доменные объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если какой-либо объект
     * не существует
     * @param ids идентификаторы доменных объектов для удаления
     * @return количество удаленных объектов
     */
    int delete(Collection<Id> ids);

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
