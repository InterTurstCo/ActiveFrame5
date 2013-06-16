package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;

import java.util.List;

/**
 * DAO для работы с бизнесс объектами выполняет операции создания, модификации,
 * удаления, поиска и т.д.
 *
 */
public interface CrudServiceDAO {


    /**
     * Создает новый бизнес-объект
     * @param domainObject
     *            бизнес-объект который будет создан
     * @param domainObjectConfig
     *            конфигурация бизнесс объекта
     * @return созданыый бизнесс объект
     */
    public DomainObject create(DomainObject domainObject, DomainObjectConfig domainObjectConfig);

    /**
     * Модифицирует переданный бизнес-объект
     * @param domainObject
     *            бизнес-объект который надо изменить
     * @param domainObjectConfig
     *            конфигурация бизнесс объекта
     * @return возвращет модифицированный бизнесс объект
     * @throws InvalidIdException
     *             если идентификатор бизнес-объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     * @throws OptimisticLockException
     *             если объект уже был модифицирован другим пользователем
     */
    public DomainObject update(DomainObject domainObject, DomainObjectConfig domainObjectConfig)
            throws InvalidIdException, ObjectNotFoundException, OptimisticLockException;

    /**
     * Удаляет бизнес-объект по уникальному идентифткатору
     * @param id
     *            уникальный идентификатор объекта который надо удалить
     * @param domainObjectConfig
     *            конфигурация бизнесс-объекта
     * @throws InvalidIdException
     *             если идентификатор бизнес-объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     */
    public void delete(Id id, DomainObjectConfig domainObjectConfig) throws InvalidIdException, ObjectNotFoundException;

    /**
     * Проверяет существует ли бизнес-объект с переданным уникальным
     * идентификатором
     * @param id
     *            идентификатор бизнес-объекта
     * @param domainObjectConfig
     *            конфигурация бизнес-объекта
     * @throws InvalidIdException
     *             если идентификатор бизнес-объекта не корректный (не поддерживается или нулевой)
     * @return true если объект существует иначе возвращает false
     */
    public boolean exists(Id id, DomainObjectConfig domainObjectConfig) throws InvalidIdException;

    /**
     * Поиск коллекции бизнес-объектов, используя фильтры и сортировку
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции.
     * @param ilterValues значения пераметров фильтров
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных бизнес-объектов
     * @return
     */
    IdentifiableObjectCollection findCollection(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues, SortOrder sortOrder, int offset, int limit);

    /**
     * Поиск количества записей в коллекции бизнес-объектов используя фильтры
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции
     * @return
     */
    int findCollectionCount(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues);

    /**
     * Поиск бизнес-объекта по уникальному идентификатору в системе.
     * @param id идентификатору бизнес-объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    DomainObject find(Id id);

    /**
     * Поиск списка бизнес-объектов по уникальным идентификаторам в системе.
     * @param ids уникальные идентификаторы
     * @return {@link List< ru.intertrust.cm.core.business.api.dto.DomainObject >}
     */
    List<DomainObject> find(List<Id> ids);
}
