package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * Сервис, обеспечивающий базовые CRUD-операции (C-Create-Создание R-Read-Чтение U-Update-Модификация D-Delete-Удаление)
 * над доменными объектами. Операции над наборами доменных объектов выполняются в рамках единой транзакции. В случае
 * возникновения исключительной ситуации, следует учитывать факт того, что, например, набор доменных объектов может быть
 * сохранен лишь частично, поэтому принудительная фиксация (commit) транзакции должна быть тщательно взвешена
 * для подобных случаев. Обычной практикой при возникновении исключений в CRUD-операциях является откат транзакции.
 * Система не гарантирует последовательности сохранения/модификации/удаления наборов доменных объектов. Например, если
 * сохраняются объекты {1, 2, 3}, то последовательность, в который они попадут в хранилище может быть {3, 2, 1}. Однако
 * результат сохранения будет возвращён методом в соответствии с оригинальным порядком объектов: {1, 2, 3},
 * если метод явно не специфицирует другое поведение.
 * <p/>
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 22:01
 */
public interface CrudService extends CrudServiceDelegate {

    public interface Remote extends CrudService {
    }

    /**
     * Возвращает true, если доменный объект существует и false в противном случае. Проверка выполняется без учета
     * проверки прав!
     * @param id уникальный идентификатор доменного объекта в системе
     * @return true, если доменный объект существует и false в противном случае
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @throws InvalidIdException если идентификатор доменного объекта не корректный (не поддерживается или нулевой)
     */
    boolean exists(Id id, DataSourceContext dataSource);

    /**
     * Возвращает доменный объект по его уникальному идентификатору в системе
     *
     * @param id уникальный идентификатор доменного объекта в системе
     * @return доменный объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException,     если id есть null
     * @throws ObjectNotFoundException,  если объект не найден
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @throws AccessException,          если отказано в доступе к объекту
     */
    DomainObject find(Id id, DataSourceContext dataSource);

    /**
     * Возвращает доменные объекты по их уникальным идентификаторам в системе.
     *
     * @param ids уникальные идентификаторы доменных объектов в системе
     * @return список найденных доменных объектов, упорядоченный аналогично оригинальному. Не найденные доменные объекты
     *         в результирующем списке представлены null.
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    List<DomainObject> find(List<Id> ids, DataSourceContext dataSource);

    /**
     * Получает все доменные объекты по типу. Возвращает как объекты указанного типа так и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return список всех доменных объектов указанного типа
     */
    List<DomainObject> findAll(String domainObjectType, DataSourceContext dataSource);

    /**
     * Получает все доменные объекты по типу. В зависимости от значения {@code exactType} возвращает
     * только объекты указанного типа или также и объекты типов-наследников
     *
     * @param domainObjectType тип доменного объекта
     * @param exactType если {@code true}, то метод возвращает только объекты указанного типа,
     *                  в противном случае - также и объекты типов-наследников
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return список всех доменных объектов указанного типа
     */
    List<DomainObject> findAll(String domainObjectType, boolean exactType, DataSourceContext dataSource);

    /**
     * Получает список связанных доменных объектов по типу объекта и указанному полю.
     * Возвращает как объекты указанного типа так и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               DataSourceContext dataSource);

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
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return список связанных доменных объектов
     */
    List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField,
                                               boolean exactType, DataSourceContext dataSource);

    /**
     * Получает список идентификаторов связанных доменных объектов по типу объекта и указанному полю.
     * Возвращает как объекты указанного типа так и объекты типов-наследников.
     * Если связанные объекты отсутствуют, возвращает пустой список (не null)
     *
     * @param domainObjectId уникальный идентификатор доменного объекта в системе
     * @param linkedType     тип доменного объекта в системе
     * @param linkedField    название поля по которому связан объект
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                        DataSourceContext dataSource);

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
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return список идентификаторов связанных доменных объектов
     */
    List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
                                        boolean exactType, DataSourceContext dataSource);

    /**
     * Возвращает доменный объект по его уникальному ключу
     *
     * @param domainObjectType типа доменного объекта
     * @param uniqueKeyValuesByName Map с наименованиями и значениями ключа
     * @param dataSource     контекст источника данных, используемого для операций с данными
     * @return доменный объект
     * @throws ObjectNotFoundException,  если объект не найден
     * @throws AccessException,          если отказано в доступе к объекту
     */
    DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName,
                                 DataSourceContext dataSource);
}
