package ru.intertrust.cm.core.business.impl;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.CrudServiceDelegate;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

/**
 * Нетранзакционная версия {@link ru.intertrust.cm.core.business.api.CrudServiceDelegate}
 *
 * @author skashanski
 */
@Stateless
@Local(CrudServiceDelegate.class)
@Remote(CrudServiceDelegate.Remote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
public class NonTransactionalCrudServiceImpl extends CrudServiceBaseImpl {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(NonTransactionalCrudServiceImpl.class);


    /**
     * Возвращает true, если доменный объект существует и false в противном случае. Проверка выполняется без учета
     * проверки прав!
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @return true, если доменный объект существует и false в противном случае
     * @throws ru.intertrust.cm.core.dao.exception.InvalidIdException если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     */
    @Override
    public boolean exists(Id id) {
        return super.exists(id);
    }

    /**
     * Возвращает доменный объект по его уникальному идентификатору в системе
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @return доменный объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException,                                если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public DomainObject find(Id id) {
        return super.find(id);
    }

    /**
     * Возвращает доменные объекты по их уникальным идентификаторам в системе.
     *
     * @param ids уникальные идентификаторы доменных объектов в системе
     * @return список найденных доменных объектов, упорядоченный аналогично оригинальному. Не найденные по каким-либо причинам доменные объекты
     *         в результирующем списке отсутствуют.
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    @Override
    public List<DomainObject> find(List<Id> ids) {
        return super.find(ids);
    }

    /**
     * Получает все доменные объекты по типу. Возвращает как объекты указанного типа так и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @return список всех доменных объектов указанного типа
     */
    @Override
    public List<DomainObject> findAll(String domainObjectType) {
        return super.findAll(domainObjectType);
    }

    /**
     * Получает все доменные объекты по типу. В зависимости от значения {@code exactType} возвращает
     * только объекты указанного типа или также и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType        если {@code true}, то метод возвращает только объекты указанного типа,
     *                         в противном случае - также и объекты типов-наследников
     * @return список всех доменных объектов указанного типа
     */
    @Override
    public List<DomainObject> findAll(String domainObjectType, boolean exactType) {
        return super.findAll(domainObjectType, exactType);
    }

    /**
     * Получает список связанных доменных объектов по типу объекта и указанному полю.
     * Возвращает как объекты указанного типа так и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @return список связанных доменных объектов
     */
    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField) {
        return super.findLinkedDomainObjects(domainObjectId, linkedType, linkedField);
    }

    /**
     * Получает список связанных доменных объектов по типу объекта и указанному полю.
     * В зависимости от значения {@code exactType} возвращает только объекты указанного типа или
     * также и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @param exactType      если {@code true}, то метод возвращает только объекты указанного типа,
     *                       в противном случае - также и объекты типов-наследников
     * @return список связанных доменных объектов
     */
    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, boolean exactType) {
        return super.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType);
    }

    /**
     * Получает список идентификаторов связанных доменных объектов по типу объекта и указанному полю.
     * Возвращает как объекты указанного типа так и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @return список идентификаторов связанных доменных объектов
     */
    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField) {
        return super.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField);
    }

    /**
     * Получает список идентификаторов связанных доменных объектов по типу объекта и указанному полю.
     * В зависимости от значения {@code exactType} возвращает только объекты указанного типа или
     * также и объекты типов-наследников
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @param exactType      если {@code true}, то метод возвращает только объекты указанного типа,
     *                       в противном случае - также и объекты типов-наследников
     * @return список идентификаторов связанных доменных объектов
     */
    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, boolean exactType) {
        return super.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType);
    }

    /**
     * Возвращает строковый тип доменного объекта по идентификатору
     *
     * @param id идентификатор доменного объекта
     * @return строковый тип доменного объекта
     */
    @Override
    public String getDomainObjectType(Id id) {
        return super.getDomainObjectType(id);
    }

    /**
     * Возвращает доменный объект по его уникальному ключу
     *
     * @param domainObjectType      типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @return доменный объект
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName) {
        return super.findByUniqueKey(domainObjectType, uniqueKeyValuesByName);
    }

    @Override
    public IdentifiableObject createIdentifiableObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainObject createDomainObject(String name) {
        return super.createDomainObject(name);//throw new UnsupportedOperationException();
    }

    @Override
    public DomainObject save(DomainObject domainObject) {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainObject findAndLock(Id id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Id id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(List<Id> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainObject findAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName) {
        throw new UnsupportedOperationException();
    }
}
