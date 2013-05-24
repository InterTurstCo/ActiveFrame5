package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.*;

import javax.ejb.Local;
import java.util.Collection;
import java.util.List;

/**
 * Сервис, обеспечивающий базовые CRUD-операции (C-Create-Создание R-Read-Чтение U-Update-Модификация D-Delete-Удаление
 * над бизнес-объектами). Операции над наборами бизнес-объектов выполняются в рамках единой транзакции. В случае
 * возникновения исключительной ситуации, следует учитывать факт того, что, например, набор бизнес-объектов может быть
 * сохранен лишь частично, поэтому принудительная фиксация (commit) транзакции должна быть тщательно взвешена
 * для подобных случаев. Обычной практикой при возникновении исключений в CRUD-операциях является откат транзакции.
 * Система не гарантирует последовательности сохранения/модификации/удаления наборов бизнес-объектов. Например, если
 * сохраняются объекты {1, 2, 3}, то последовательность, в который они попадут в хранилище может быть {3, 2, 1}. Однако
 * результат сохранения будет возвращён методом в соответствии с оригинальным порядком объектов: {1, 2, 3},
 * если метод явно не специфицирует другое поведение.
 *
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 22:01
 */
@Local
public interface CrudService {
    @javax.ejb.Remote
    public static interface Remote extends CrudService {
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
     * Создаёт бизнес-объект определённого типа, не сохраняя в СУБД. Заполняет необходимые атрибуты значениями,
     * сгенерированными согласно правилам, определённым для данного объекта. Идентификатор объекта при этом
     * не генерируется.
     *
     * @param name название бизнес-объекта, который нужно создать
     * @return сохранённый бизнес-объект
     * @throws IllegalArgumentException, если бизнес-объекта данного типа не существует
     * @throws NullPointerException, если type есть null.
     */
    BusinessObject createBusinessObject(String name);

    /**
     * Сохраняет бизнес-объект. Если объект не существует в системе, создаёт его и заполняет отсутствующие атрибуты
     * значениями, сгенерированными согласно правилам, определённым для данного объекта (например, будет сгенерирован и
     * заполнен идентификатор объекта). Оригинальный Java-объект измененям не подвергается, изменения отражены в
     * возвращённом объекте.
     *
     * @param businessObject бизнес-объект, который нужно сохранить
     * @return сохранённый бизнес-объект
     * @throws IllegalArgumentException, если состояние объекта не позволяет его сохранить (например, если атрибут
     * содержит данные неверного типа, или обязательный атрибут не определён)
     * @throws NullPointerException, если бизнес-объект есть null.
     */
    BusinessObject save(BusinessObject businessObject);

    /**
     * Сохраняет список бизнес-объектов. Если какой-то объект не существует в системе, создаёт его и заполняет
     * отсутствующие атрибуты значениями, сгенерированными согласно правилам, определённым для данного объекта
     * (например, будет сгенерирован и заполнен идентификатор объекта). Оригинальные Java-объекты измененям
     * не подвергаются, изменения отражены в возвращённых объектах.
     *
     * @param businessObjects список бизнес-объектов, которые нужно сохранить
     * @return список сохранённых бизнес-объектов, упорядоченный аналогично оригинальному
     * @throws IllegalArgumentException, если состояние хотя бы одного объекта не позволяет его сохранить (например,
     * если атрибут содержит данные неверного типа, или обязательный атрибут не определён)
     * @throws NullPointerException, если список или хотя бы один бизнес-объект в списке есть null
     */
    List<BusinessObject> save(List<BusinessObject> businessObjects);

    /**
     * Возвращает true, если бизнес-объект существует и false в противном случае.
     *
     * @param id уникальный идентификатор бизнес-объекта в системе
     * @return true, если бизнес-объект существует и false в противном случае
     * @throws NullPointerException, если id есть null
     */
    boolean exists(Id id);

    /**
     * Возвращает бизнес-объект по его уникальному идентификатору в системе
     *
     * @param id уникальный идентификатор бизнес-объекта в системе
     * @return бизнес-объект с данным идентификатором или null, если объект не существует
     * @throws NullPointerException, если id есть null
     */
    BusinessObject find(Id id);

    /**
     * Возвращает бизнес-объекты по их уникальным идентификаторам в системе.
     *
     * @param ids уникальные идентификаторы бизнес-объектов в системе
     * @return список найденных бизнес-объектов, упорядоченный аналогично оригинальному. Не найденные бизнес-объекты
     * в результирующем списке представлены null.
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    List<BusinessObject> find(List<Id> ids);

    /**
     * Возвращает коллекцию, отфильтрованную и упорядоченную согласно критериям
     *
     * @param collectionName название коллекции
     * @param filters фильтры
     * @param sortOrder порядок сортировки коллекции
     * @param limit максимальное количесвто возвращаемых бизнес-объектов
     * @return коллекцию
     */
    IdentifiableObjectCollection findCollection(String collectionName, List<Filter> filters, SortOrder sortOrder, int offset, int limit);

    /**
     * Возвращает коллекцию, упорядоченную согласно заданному порядку
     *
     * @param collectionName название коллекции
     * @param filters фильтры
     * @param sortOrder порядок сортировки коллекции
     * @return коллекцию
     */
    int findCollectionCount(String collectionName, List<Filter> filters, SortOrder sortOrder);

    /**
     * Удаляет бизнес-объект по его уникальному идентификатору. Не осуществляет никаких действий, если объект
     * не существует
     *
     * @param id уникальный идентификатор бизнес-объекта в системе
     * @throws NullPointerException, если id есть null
     */
    void delete(Id id);

    /**
     * Удаляет бизнес-объекты по их уникальным идентификаторам. Не осуществляет никаких действий, если какой-либо объект
     * не существует
     *
     * @param ids уникальные идентификаторы бизнес-объектов, которых необходимо удалить, в системе
     * @return количество удалённых бизнес-объектов
     * @throws NullPointerException, если список или хотя бы один идентификатор в списке есть null
     */
    int delete(Collection<Id> ids);

    /**
     * Удаляет все бизнес-объекты с заданным названием
     *
     * @param businessObjectName название бизнес-объекта
     * @return количество удалённых бизнес-объектов
     */
    int deleteAll(String businessObjectName);
}
