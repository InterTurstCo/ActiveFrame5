package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.List;
import java.util.Map;

/**
 * Внутренний служебный интерфейс CRUD-сервиса. Используется для создания транзакционной и нетранзакциооной версий сервиса
 * <p/>
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 22:01
 */
public interface CrudServiceDelegate {

    public interface Remote extends CrudServiceDelegate {
    }

    /**
     * Создаёт идентифицируемый объект. Заполняет необходимые атрибуты значениями, сгенерированными согласно правилам.
     * Идентификатор объекта при этом не определяется.
     * См. некое описание
     *
     * @return новый идентифицируемый объект
     */
    IdentifiableObject createIdentifiableObject();

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
    DomainObject createDomainObject(String name);

    /**
     * Сохраняет доменный объект. Если объект не существует в системе, создаёт его и заполняет отсутствующие атрибуты
     * значениями, сгенерированными согласно правилам, определённым для данного объекта (например, будет сгенерирован и
     * заполнен идентификатор объекта). Оригинальный Java-объект измененям не подвергается, изменения отражены в
     * возвращённом объекте.
     *
     * @param domainObject доменный объект, который нужно сохранить
     * @return сохранённый доменный объект
     * @throws IllegalArgumentException, если состояние объекта не позволяет его сохранить (например, если атрибут
     *                                   содержит данные неверного типа, или обязательный атрибут не определён)
     * @throws NullPointerException,     если доменный объект есть null.
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException,  если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,          если отказано в доступе к объекту
     */
    DomainObject save(DomainObject domainObject);

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
    List<DomainObject> save(List<DomainObject> domainObjects);

    /**
     * Возвращает true, если доменный объект существует и false в противном случае. Проверка выполняется без учета
     * проверки прав!
     * @param id уникальный идентификатор доменного объекта в системе
     * @return true, если доменный объект существует и false в противном случае
     * @throws ru.intertrust.cm.core.dao.exception.InvalidIdException если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     */
    boolean exists(Id id);

    /**
     * Возвращает доменный объект по его уникальному идентификатору в системе
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @return доменный объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException,     если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException,  если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,          если отказано в доступе к объекту
     */
    DomainObject find(Id id);

    /**
     * Блокирует и возвращает доменный объект по его уникальному идентификатору в системе
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @return доменный объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException, если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException,  если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,          если отказано в доступе к объекту
     */
    DomainObject findAndLock(Id id);

    /**
     * Возвращает доменные объекты по их уникальным идентификаторам в системе.
     *
     * @param ids уникальные идентификаторы доменных объектов в системе
     * @return список найденных доменных объектов, упорядоченный аналогично оригинальному. Не найденные по каким-либо причинам доменные объекты
     *         в результирующем списке отсутствуют.
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    List<DomainObject> find(List<Id> ids);

    /**
     * Получает все доменные объекты по типу. Возвращает как объекты указанного типа так и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @return список всех доменных объектов указанного типа
     */
    List<DomainObject> findAll(String domainObjectType);

    /**
     * Получает все доменные объекты по типу. В зависимости от значения {@code exactType} возвращает
     * только объекты указанного типа или также и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType если {@code true}, то метод возвращает только объекты указанного типа,
     *                  в противном случае - также и объекты типов-наследников
     * @return список всех доменных объектов указанного типа
     */
    List<DomainObject> findAll(String domainObjectType, boolean exactType);

    /**
     * Удаляет доменный объект по его уникальному идентификатору. Не осуществляет никаких действий, если объект
     * не существует
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @throws NullPointerException, если id есть null
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException,  если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,          если отказано в доступе к объекту
     */
    void delete(Id id);

    /**
     * Удаляет доменные объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если какой-либо объект
     * не существует
     *
     * @param ids уникальные идентификаторы доменных объектов, которых необходимо удалить, в системе
     * @return количество удалённых доменных объектов
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    int delete(List<Id> ids);

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
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField);

    /**
     * Получает список связанных доменных объектов по типу объекта и указанному полю.
     * В зависимости от значения {@code exactType} возвращает только объекты указанного типа или
     * также и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @param exactType если {@code true}, то метод возвращает только объекты указанного типа,
     *                  в противном случае - также и объекты типов-наследников
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType);

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
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField);

    /**
     * Получает список идентификаторов связанных доменных объектов по типу объекта и указанному полю.
     * В зависимости от значения {@code exactType} возвращает только объекты указанного типа или
     * также и объекты типов-наследников
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @param exactType если {@code true}, то метод возвращает только объекты указанного типа,
     *                  в противном случае - также и объекты типов-наследников
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                        boolean exactType);

    /**
     * Возвращает строковый тип доменного объекта по идентификатору
     * @param id идентификатор доменного объекта
     * @return строковый тип доменного объекта
     */
    String getDomainObjectType(Id id);


    /**
     * Возвращает доменный объект по его уникальному ключу
     *
     * @param domainObjectType типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @return доменный объект
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException,  если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,          если отказано в доступе к объекту
     */
    DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName);

    /**
     * Блокирует и возвращает доменный объект по его уникальному ключу
     *
     * @param domainObjectType типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @return доменный объект
     * @throws ru.intertrust.cm.core.model.ObjectNotFoundException,  если объект не найден
     * @throws ru.intertrust.cm.core.model.AccessException,          если отказано в доступе к объекту
     */
    DomainObject findAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName);

    /**
     * Установка статуса доменного объекта. Разрешается выполнять только под правами администратора
     * @param id идентификатор доменного объекта
     * @param status имя статуса
     * @return
     */
    DomainObject setStatus(Id id, String status);

}
