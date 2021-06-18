package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * DAO для работы с доменными объектами выполняет операции создания, модификации,
 * удаления, поиска и т.д.
 *
 */
public interface DomainObjectDao {

    enum AutoCreatedType {

        UserEventLog("user_event_log"), ObjectAccessLog("object_access_log");

        private String typeName;

        AutoCreatedType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    String REFERENCE_TYPE_POSTFIX = "_type";
    String TIME_ID_ZONE_POSTFIX = "_tz";
    String REFERENCE_POSTFIX = "_id";

    String ID_COLUMN = "id";
    String TYPE_COLUMN = "id_type";
    String CREATED_DATE_COLUMN = "created_date";
    String UPDATED_DATE_COLUMN = "updated_date";
    String CREATED_BY = "created_by";
    String CREATED_BY_TYPE_COLUMN = CREATED_BY + REFERENCE_TYPE_POSTFIX;    
    String UPDATED_BY = "updated_by";
    String UPDATED_BY_TYPE_COLUMN = UPDATED_BY + REFERENCE_TYPE_POSTFIX;
    
    String ACCESS_OBJECT_ID = "access_object_id";
    String SECURITY_STAMP_COLUMN = "security_stamp";
    String SECURITY_STAMP_TYPE_COLUMN = SECURITY_STAMP_COLUMN + REFERENCE_TYPE_POSTFIX;
    String STATUS_TYPE_COLUMN = GenericDomainObject.STATUS_FIELD_NAME + REFERENCE_TYPE_POSTFIX;
    String OPERATION_COLUMN = "operation";

    String DOMAIN_OBJECT_ID_COLUMN = "domain_object_id";
    String DOMAIN_OBJECT_ID_TYPE_COLUMN = "domain_object_id_type";
    
    String COMPONENT_COLUMN = "component";
    String IP_ADDRESS_COLUMN = "ip_address";
    String INFO_COLUMN = "info";

    /**
     * Создает новый доменный объект. Метод вызывается только с системным маркером доступа.
     *
     * @param domainObject доменный объект который будет создан
     * @param accessToken маркер доступа
     * @return созданыый доменный объект
     */
    public DomainObject create(DomainObject domainObject, AccessToken accessToken);

    /**
     * Сохраняет доменный объект. Если объект не существует в системе, создаёт его и заполняет отсутствующие атрибуты
     * значениями, сгенерированными согласно правилам, определённым для данного объекта (например, будет сгенерирован и
     * заполнен идентификатор объекта). Оригинальный Java-объект измененям не подвергается, изменения отражены в
     * возвращённом объекте. Маркер доступа должен позволять изменение для сущесвующего объекта и быть системным
     * для нового.
     *
     * @param domainObject
     * @param accessToken маркер доступа
     * @return сохраненный доменный объект
     */
    DomainObject save(DomainObject domainObject, AccessToken accessToken);

    /**
     * Сохраняет список доменных объектов. Если какой-то объект не существует в системе, создаёт его и заполняет
     * отсутствующие атрибуты значениями, сгенерированными согласно правилам, определённым для данного объекта
     * (например, будет сгенерирован и заполнен идентификатор объекта). Оригинальные Java-объекты измененям
     * не подвергаются, изменения отражены в возвращённых объектах.
     *
     * @param domainObjects доменные объекты для сохранения
     * @return список сохраненных доменных объектов
     */
    List<DomainObject> save(List<DomainObject> domainObjects, AccessToken accessToken);

    /**
     * Удаляет доменный объект по уникальному идентифткатору.
     *
     * @param id уникальный идентификатор объекта который надо удалить
     * @throws InvalidIdException если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException если не существует объекта с таким идентификатором
     */
    public void delete(Id id, AccessToken accessToken) throws InvalidIdException, ObjectNotFoundException;

    /**
     * Удаляет доменные объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если
     * какой-либо объект не существует.
     *
     * @param ids идентификаторы доменных объектов для удаления
     * @return количество удаленных объектов
     */
    int delete(List<Id> ids, AccessToken accessToken);

    /**
     * Удаляет доменные объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если
     * какой-либо объект не существует.
     *
     * <p><b>Отличается от обычного {@link #delete(List, AccessToken)} тем, что происходит только удаление из БД:
     * без аудит-логов, проверок, очистки кэша и т.п. БЕЗ вызова точек расширения и пересчета групп доступа!
     * Введен для удаления очень больших объемов данных там, где все это не нужно, например, в ИРШК.</b>
     * 
     * @param ids идентификаторы доменных объектов для удаления
     * @return количество удаленных объектов
     */
    int deleteQuickly(List<Id> ids, AccessToken accessToken);

    /**
     * Проверяет существует ли доменный объект с переданным уникальным идентификатором. Проверка выполняется без учета
     * проверки прав.
     * @param id идентификатор доменного объекта
     * @return true если объект существует иначе возвращает false
     * @throws InvalidIdException если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     */
    public boolean exists(Id id) throws InvalidIdException;

    /**
     * Поиск доменного объекта по уникальному идентификатору в системе.
     *
     * @param id идентификатору доменного объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    DomainObject find(Id id, AccessToken accessToken);

    /**
     * Поиск и блокировка доменного объекта по уникальному идентификатору в системе.
     *
     * @param id идентификатору доменного объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    DomainObject findAndLock(Id id, AccessToken accessToken);

    /**
     * Поиск списка доменных объектов по уникальным идентификаторам в системе.
     * Идентификаторы могут быть разных типов доменных объектов.
     *
     * @param ids уникальные идентификаторы
     * @return {@link List< ru.intertrust.cm.core.business.api.dto.DomainObject >}
     */
    List<DomainObject> find(List<Id> ids, AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе, типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     * Возвращает как объекты указаннаго типа так и типов-наследников
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе, типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     * Возвращает как объекты указаннаго типа так и типов-наследников
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId,
                                               String linkedType, String linkedField, int offset, int limit,
                                               AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе,типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @param exactType возвращать только объекты типа {@code linkedType}, не возвращать объекты наследованных типов
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType, AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе, типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю с указанием максимального количества возвращаемых объектов и
     * отступа
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @param exactType возвращать только объекты типа {@code linkedType}, не возвращать объекты наследованных типов
     * @param offset отступ
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType, int offset, int limit, AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе, типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю с указанием максимального количества возвращаемых объектов и
     * отступа.
     * Возвращает как идентификаторы указанного типа так и типов-наследников
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId,
                                        String linkedType, String linkedField, AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе, типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю с указанием максимального количества возвращаемых объектов и
     * отступа.
     * Возвращает как идентификаторы указанного типа так и типов-наследников
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId,
                                        String linkedType, String linkedField, int offset, int limit,
                                        AccessToken accessToken);

    /**
     * Поиск списка идентификаторов связанных доменных объектов по уникальному идентификатору владельца в системе,
     * типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @param exactType возвращать идентификаторы только объектов типа {@code linkedType},
     *                  не возвращать идентификаторы объектов наследованных типов
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            AccessToken accessToken);

    /**
     * Поиск списка идентификаторов связанных доменных объектов по уникальному идентификатору владельца в системе,
     * типу дочернего (запрашиваемого) доменного объекта и указанному полю с указанием максимального количества
     * возвращаемых объектов и отступа.
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @param exactType возвращать идентификаторы только объектов типа {@code linkedType},
     *                  не возвращать идентификаторы объектов наследованных типов
     * @param offset отступ
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType,
                                        int offset, int limit, AccessToken accessToken);

    /**
     * Поиск всех доменного объектов указанного типа в системе.
     * Возвращает как объекты указанного типа так и типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    List<DomainObject> findAll(String domainObjectType, AccessToken accessToken);

    /**
     * Поиск всех доменного объектов указанного типа в системе.
     * Возвращает как объекты указанного типа так и типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    List<DomainObject> findAll(String domainObjectType, int offset, int limit, AccessToken accessToken);

    /**
     * Поиск всех доменного объектов указанного типа в системе.
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType возвращать только объекты типа {@code linkedType}, не возвращать объекты наследованных типов
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    List<DomainObject> findAll(String domainObjectType, boolean exactType, AccessToken accessToken);

    /**
     * Поиск доменных объектов указанного типа в системе с указанием максимального количества возвращаемых объектов и
     * отступа.
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType возвращать только объекты типа {@code linkedType}, не возвращать объекты наследованных типов
     * @param offset отступ
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    List<DomainObject> findAll(String domainObjectType, boolean exactType, int offset, int limit,
                               AccessToken accessToken);

    /**
     * Устанавливает статус доменного объекта. Метод может быть вызван только с системным маркером доступа.
     *
     * @param objectId идентификатор доменного объекта
     * @param status идентификатор статуса
     * @param accessToken маркер доступа
     */
    DomainObject setStatus(Id objectId, Id status, AccessToken accessToken);

    /**
     * Возвращает доменный объект по его уникальному ключу
     *
     *
     * @param domainObjectType типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @return ID доменного объекта
     * @throws ObjectNotFoundException,  если объект не найден
     */
    DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken);

    /**
     * Возвращает и блокирует доменный объект по его уникальному ключу
     *
     *
     * @param domainObjectType типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @return ID доменного объекта
     * @throws ObjectNotFoundException,  если объект не найден
     */
    DomainObject finAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken);

    /**
     * Создаёт ID доменного объекта по его типу и числовому идентификатору. Не рекомендуется к использованию, за исключением
     * экстренных случаев. Пример такого случая - создание Id по access_object_id, который в доменных объектах представлен в виде числа, а не Id.
     * @param type тип доменного объекта
     * @param id числовой идентификатор доменного объекта
     * @return ID доменного объекта
     */
    @Deprecated
    Id createId(String type, long id);
}
