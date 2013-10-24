package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessToken;
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

    String ID_COLUMN = "ID";
    String TYPE_COLUMN = "TYPE_ID";
    String CREATED_DATE_COLUMN = "CREATED_DATE";
    String UPDATED_DATE_COLUMN = "UPDATED_DATE";
    String STATUS_COLUMN = "STATUS";    
    String REFERENCE_TYPE_POSTFIX = "_TYPE";
    String STATUS_TYPE_COLUMN = STATUS_COLUMN  + REFERENCE_TYPE_POSTFIX;
    String STATUS_DO = "Status";    

    String DOMAIN_OBJECT_ID = "DOMAIN_OBJECT_ID";
    String COMPONENT = "COMPONENT";
    String IP_ADDRESS = "IP_ADDRESS";
    String INFO = "INFO";
    String OPERATION_COLUMN = "OPERATION";

    /**
     * Создает новый доменный объект
     * @param domainObject
     *            доменный объект который будет создан
     * @return созданыый доменный объект
     */
    public DomainObject create(DomainObject domainObject);

    /**
     * Модифицирует переданный доменный объект
     *
     * @param domainObject доменный объект который надо изменить
     * @return возвращет модифицированный доменный объект
     * @throws InvalidIdException      если идентификатор доменный объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException если не существует объекта с таким идентификатором
     * @throws OptimisticLockException если объект уже был модифицирован другим пользователем
     */
    public DomainObject update(DomainObject domainObject)
            throws InvalidIdException, ObjectNotFoundException, OptimisticLockException;

    /**
     * Сохраняет доменный объект. Если объект не существует в системе, создаёт его и заполняет отсутствующие атрибуты
     * значениями, сгенерированными согласно правилам, определённым для данного объекта (например, будет сгенерирован и
     * заполнен идентификатор объекта). Оригинальный Java-объект измененям не подвергается, изменения отражены в
     * возвращённом объекте.
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
     * @return список сохраненных доменныъ лбъектов
     */
    List<DomainObject> save(List<DomainObject> domainObjects);

    /**
     * Удаляет доменный объект по уникальному идентифткатору
     *
     * @param id уникальный идентификатор объекта который надо удалить
     * @throws InvalidIdException      если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     * @throws ObjectNotFoundException если не существует объекта с таким идентификатором
     */
    public void delete(Id id) throws InvalidIdException, ObjectNotFoundException;

    /**
     * Удаляет доменные объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если какой-либо объект
     * не существует
     *
     * @param ids идентификаторы доменных объектов для удаления
     * @return количество удаленных объектов
     */
    int delete(Collection<Id> ids);

    /**
     * Проверяет существует ли доменный объект с переданным уникальным
     * идентификатором
     *
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
     * Поиск списка доменных объектов по уникальным идентификаторам в системе. Причем идентификаторы могут быть разных типов доменных объектов.
     *
     * @param ids уникальные идентификаторы
     * @return {@link List< ru.intertrust.cm.core.business.api.dto.DomainObject >}
     */
    List<DomainObject> find(List<Id> ids, AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе,типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken);

    /**
     * Поиск списка связанных доменных объектов по уникальному идентификатору владельца в системе, типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю с указанием максимального количества возвращаемых объектов и
     * отступа
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @param offset отступ
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               int offset, int limit, AccessToken accessToken);

    /**
     * Поиск списка идентификаторов связанных доменных объектов по уникальному идентификатору владельца в системе,
     * типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken);

    /**
     * Поиск списка идентификаторов связанных доменных объектов по уникальному идентификатору владельца в системе,
     * типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю с указанием максимального количества возвращаемых объектов и
     * отступа
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @param offset отступ
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                        int offset, int limit, AccessToken accessToken);

    /**
     * Поиск всех доменного объектов указанного типа в системе.
     *
     * @param domainObjectType тип доменного объекта
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    List<DomainObject> findAll(String domainObjectType, AccessToken accessToken);

    /**
     * Поиск доменных объектов указанного типа в системе с указанием максимального количества возвращаемых объектов и
     * отступа
     *
     * @param domainObjectType тип доменного объекта
     * @param offset отступ
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     */
    List<DomainObject> findAll(String domainObjectType, int offset, int limit, AccessToken accessToken);
    
    /**
     * Устанавливает статус доменного объекта
     * @param objectId идентификатор доменного объекта
     * @param status идентификатор статуса
     */
    DomainObject setStatus(Id objectId, Long status);
}
