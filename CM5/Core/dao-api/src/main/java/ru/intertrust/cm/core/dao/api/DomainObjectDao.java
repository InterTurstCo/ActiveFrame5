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
 */
public interface DomainObjectDao {


    /**
     * Создает новый доменный объект
     *
     * @param domainObject доменный объект который будет создан
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
     * @return сохраненный доменный объект
     */
    DomainObject save(DomainObject domainObject);

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
     * Поиск списка вложенных доменных объектов по уникальному идентификатору владельца в системе и типу дочернего
     * (запрашиваемого) доменного объекта.
     *
     * @param domainObjectId - id идентификатор доменного объекта владельца вложений
     * @param childType      - тип вложенного (дочернего) доменного объекта
     * @return
     */
    @Deprecated
    List<DomainObject> findChildren(Id domainObjectId, String childType, AccessToken accessToken);

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
     * Поиск списка идентификаторов связанных доменных объектов по уникальному идентификатору владельца в системе,типу дочернего
     * (запрашиваемого) доменного объекта и указанному полю.
     *
     * @param domainObjectId идентификатор доменного объекта, владельца вложений
     * @param linkedType     тип связанного (дочернего) доменного объекта
     * @param linkedField    название поля, которым связаны объекты
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken);

}
