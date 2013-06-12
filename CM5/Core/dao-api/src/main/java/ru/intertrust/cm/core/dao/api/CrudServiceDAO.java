package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.CollectionConfig;
import ru.intertrust.cm.core.config.CollectionFilterConfig;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;

/**
 * DAO для работы с бизнесс объектами выполняет операции создания, модификации,
 * удаления, поиска и т.д.
 *
 */
public interface CrudServiceDAO {

    /**
     * Генерирует уникальный идентификатор используя последоватеьность(сиквенс)
     * @param sequenceName
     *            имя последовательности в базе данных
     * @return уникальный идентификатор
     */
    public long generateNextSequence(String sequenceName);

    /**
     * Создает новый бизнес-объект
     * @param businessObject
     *            бизнес-объект который будет создан
     * @param businessObjectConfig
     *            конфигурация бизнесс объекта
     * @return созданыый бизнесс объект
     */
    public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);

    /**
     * Модифицирует переданный бизнес-объект
     * @param businessObject
     *            бизнес-объект который надо изменить
     * @param businessObjectConfig
     *            конфигурация бизнесс объекта
     * @return возвращет модифицированный бизнесс объект
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     * @throws OptimisticLockException
     *             если объект уже был модифицирован другим пользователем
     */
    public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig)
            throws ObjectNotFoundException, OptimisticLockException;

    public BusinessObject read(BusinessObject businessObjec, BusinessObjectConfig businessObjectConfig);

    /**
     * Удаляет бизнес-объект по уникальному идентифткатору
     * @param id
     *            уникальный идентификатор объекта который надо удалить
     * @param businessObjectConfig
     *            конфигурация бизнесс-объекта
     * @throws ObjectNotFoundException
     *             если не существует объекта с таким идентификатором
     */
    public void delete(Id id, BusinessObjectConfig businessObjectConfig) throws ObjectNotFoundException;

    /**
     * Проверяет существует ли бизнес-объект с переданным уникальным
     * идентификатором
     * @param id
     *            идентификатор бизнес-объекта
     * @param businessObjectConfig
     *            конфигурация бизнес-объекта
     * @return true если объект существует иначе возвращает false
     */
    public boolean exists(Id id, BusinessObjectConfig businessObjectConfig);

    /**
     * Поиск коллекции бизнес-объектов, используя фильтры и сортировку
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции.
     * @param sortOrder порядок сортировки
     * @param offset смещение
     * @param limit ограничение количества возвращенных бизнес-объектов
     * @return
     */
    IdentifiableObjectCollection findCollectionByQuery(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, SortOrder sortOrder, int offset, int limit);

    /**
     * Поиск количества записей в коллекции бизнес-объектов используя фильтры
     * @param collectionConfig конфигурация коллекции
     * @param filledFilterConfigs заполненные фильтры в конфигурации коллекции
     * @param sortOrder порядок сортировки
     * @return
     */
    int findCollectionCountByQuery(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs, SortOrder sortOrder);
    
}
