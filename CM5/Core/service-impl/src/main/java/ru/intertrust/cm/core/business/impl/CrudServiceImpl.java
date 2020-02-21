package ru.intertrust.cm.core.business.impl;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.CrudServiceDelegate;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

/**
 * Реализация сервиса для работы c базовы CRUD-операциями. Смотри link @CrudService
 *
 * @author skashanski
 */
@Stateless
@Local(CrudService.class)
@Remote(CrudService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class CrudServiceImpl implements CrudService, CrudService.Remote {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(CrudServiceImpl.class);

    @Autowired
    @Qualifier("nonTransactionalCrudService")
    private CrudServiceDelegate nonTransactionalCrudService;

    @Autowired
    @Qualifier("transactionalCrudService")
    private CrudServiceDelegate transactionalCrudService;

    /**
     * Возвращает true, если доменный объект существует и false в противном случае. Проверка выполняется без учета
     * проверки прав!
     *
     * @param id          уникальный идентификатор доменного объекта в системе
     * @throws ru.intertrust.cm.core.dao.exception.InvalidIdException если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     */
    @Override
    public boolean exists(Id id, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.exists(id);
        } else {
            return transactionalCrudService.exists(id);
        }
    }

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
        return transactionalCrudService.exists(id);
    }

    /**
     * Возвращает доменный объект по его уникальному идентификатору в системе
     *
     * @param id          уникальный идентификатор доменного объекта в системе
     * @return доменный объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException,    если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public DomainObject find(Id id, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.find(id);
        } else {
            return transactionalCrudService.find(id);
        }
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
        return transactionalCrudService.find(id);
    }

    /**
     * Возвращает доменные объекты по их уникальным идентификаторам в системе.
     *
     * @param ids         уникальные идентификаторы доменных объектов в системе
     * @return список найденных доменных объектов, упорядоченный аналогично оригинальному. Не найденные доменные объекты
     *                    в результирующем списке представлены null.
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    @Override
    public List<DomainObject> find(List<Id> ids, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.find(ids);
        } else {
            return transactionalCrudService.find(ids);
        }
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
        return transactionalCrudService.find(ids);
    }

    /**
     * Создаёт идентифицируемый объект. Заполняет необходимые атрибуты значениями, сгенерированными согласно правилам.
     * Идентификатор объекта при этом не определяется.
     * См. некое описание
     *
     * @return новый идентифицируемый объект
     */
    @Override
    public IdentifiableObject createIdentifiableObject() {
        return transactionalCrudService.createIdentifiableObject();
    }

    /**
     * Создаёт доменный объект определённого типа, не сохраняя в СУБД. Заполняет необходимые атрибуты значениями,
     * сгенерированными согласно правилам, определённым для данного объекта. Идентификатор объекта при этом
     * не генерируется.
     *
     * @param name название доменного объекта, который нужно создать
     * @return сохранённый доменного объект
     * @throws IllegalArgumentException, если доменного объекта данного типа не существует
     * @throws NullPointerException,     если type есть null.
     */
    @Override
    public DomainObject createDomainObject(String name) {
        return transactionalCrudService.createDomainObject(name);
    }

    /**
     * Сохраняет доменный объект. Если объект не существует в системе, создаёт его и заполняет отсутствующие атрибуты
     * значениями, сгенерированными согласно правилам, определённым для данного объекта (например, будет сгенерирован и
     * заполнен идентификатор объекта). Оригинальный Java-объект измененям не подвергается, изменения отражены в
     * возвращённом объекте.
     *
     * @param domainObject доменный объект, который нужно сохранить
     * @return сохранённый доменный объект
     * @throws IllegalArgumentException,                            если состояние объекта не позволяет его сохранить (например, если атрибут
     *                                                              содержит данные неверного типа, или обязательный атрибут не определён)
     * @throws NullPointerException,                                если доменный объект есть null.
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public DomainObject save(DomainObject domainObject) {
        return transactionalCrudService.save(domainObject);
    }

    /**
     * Сохраняет список доменных объектов. Если какой-то объект не существует в системе, создаёт его и заполняет
     * отсутствующие атрибуты значениями, сгенерированными согласно правилам, определённым для данного объекта
     * (например, будет сгенерирован и заполнен идентификатор объекта). Оригинальные Java-объекты измененям
     * не подвергаются, изменения отражены в возвращённых объектах.
     *
     * @param domainObjects список доменных объектов, которые нужно сохранить
     * @return список сохранённых доменных объектов, упорядоченный аналогично оригинальному
     * @throws IllegalArgumentException, если состояние хотя бы одного объекта не позволяет его сохранить (например,
     *                                   если атрибут содержит данные неверного типа, или обязательный атрибут не определён)
     * @throws NullPointerException,     если список или хотя бы один доменный объект в списке есть null
     */
    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        return transactionalCrudService.save(domainObjects);
    }

    /**
     * Блокирует и возвращает доменный объект по его уникальному идентификатору в системе
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @return доменный объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException,                                если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public DomainObject findAndLock(Id id) {
        return transactionalCrudService.findAndLock(id);
    }

    /**
     * Получает все доменные объекты по типу. Возвращает как объекты указанного типа так и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @return список всех доменных объектов указанного типа
     */
    @Override
    public List<DomainObject> findAll(String domainObjectType) {
        return transactionalCrudService.findAll(domainObjectType);
    }

    /**
     * Получает все доменные объекты по типу. Возвращает как объекты указанного типа так и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @return список всех доменных объектов указанного типа
     */
    @Override
    public List<DomainObject> findAll(String domainObjectType, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findAll(domainObjectType);
        } else {
            return transactionalCrudService.findAll(domainObjectType);
        }
    }

    /**
     * Удаляет доменный объект по его уникальному идентификатору. Не осуществляет никаких действий, если объект
     * не существует
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @throws NullPointerException,                                если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public void delete(Id id) {
        transactionalCrudService.delete(id);
    }

    /**
     * Удаляет доменные объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если какой-либо объект
     * не существует
     *
     * @param ids уникальные идентификаторы доменных объектов, которых необходимо удалить, в системе
     * @return количество удалённых доменных объектов
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    @Override
    public int delete(List<Id> ids) {
        return transactionalCrudService.delete(ids);
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
        return transactionalCrudService.findLinkedDomainObjects(domainObjectId, linkedType, linkedField);
    }

    /**
     * Получает список связанных доменных объектов по типу объекта и указанному полю.
     * Возвращает как объекты указанного типа так и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     */
    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                                      DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findLinkedDomainObjects(domainObjectId, linkedType, linkedField);
        } else {
            return transactionalCrudService.findLinkedDomainObjects(domainObjectId, linkedType, linkedField);
        }
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
     */
    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                                      boolean exactType) {
        return transactionalCrudService.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType);
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
     */
    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                                      boolean exactType, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType);
        } else {
            return transactionalCrudService.findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType);
        }
    }

    /**
     * Получает все доменные объекты по типу. В зависимости от значения {@code exactType} возвращает
     * только объекты указанного типа или также и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType        если {@code true}, то метод возвращает только объекты указанного типа,
     *                         в противном случае - также и объекты типов-наследников
     */
    @Override
    public List<DomainObject> findAll(String domainObjectType, boolean exactType) {
        return transactionalCrudService.findAll(domainObjectType, exactType);
    }

    /**
     * Получает все доменные объекты по типу. В зависимости от значения {@code exactType} возвращает
     * только объекты указанного типа или также и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType        если {@code true}, то метод возвращает только объекты указанного типа,
     *                         в противном случае - также и объекты типов-наследников
     */
    @Override
    public List<DomainObject> findAll(String domainObjectType, boolean exactType, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findAll(domainObjectType, exactType);
        } else {
            return transactionalCrudService.findAll(domainObjectType, exactType);
        }
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
        return transactionalCrudService.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField);
    }

    /**
     * Получает список идентификаторов связанных доменных объектов по типу объекта и указанному полю.
     * Возвращает как объекты указанного типа так и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     */
    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                               DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField);
        } else {
            return transactionalCrudService.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField);
        }
    }

    /**
     * Возвращает строковый тип доменного объекта по идентификатору
     *
     * @param id идентификатор доменного объекта
     * @return строковый тип доменного объекта
     */
    @Override
    public String getDomainObjectType(Id id) {
        return transactionalCrudService.getDomainObjectType(id);
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
        return transactionalCrudService.findByUniqueKey(domainObjectType, uniqueKeyValuesByName);
    }

    /**
     * Блокирует и возвращает доменный объект по его уникальному ключу
     *
     * @param domainObjectType      типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @return доменный объект
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException, если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,         если отказано в доступе к объекту
     */
    @Override
    public DomainObject findAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName) {
        return transactionalCrudService.findAndLockByUniqueKey(domainObjectType, uniqueKeyValuesByName);
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
     */
    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType) {
        return transactionalCrudService.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType);
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
     */
    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType, DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType);
        } else {
            return transactionalCrudService.findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType);
        }
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
    public DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName,
                                        DataSourceContext dataSourceContext) {
        if (DataSourceContext.CLONE.equals(dataSourceContext)) {
            return nonTransactionalCrudService.findByUniqueKey(domainObjectType, uniqueKeyValuesByName);
        } else {
            return transactionalCrudService.findByUniqueKey(domainObjectType, uniqueKeyValuesByName);
        }
    }

    /**
     * Установка статуса доменного объекта. Разрешается выполнять только под правами администратора
     * @param id идентификатор доменного объекта
     * @param status имя статуса
     * @return
     */
    @Override
    public DomainObject setStatus(Id id, String status) {
        return transactionalCrudService.setStatus(id, status);
    }

}
