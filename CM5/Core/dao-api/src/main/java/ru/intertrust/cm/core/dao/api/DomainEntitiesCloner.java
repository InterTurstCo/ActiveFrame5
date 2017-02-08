package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 08.02.2017
 *         Time: 12:39
 */
public interface DomainEntitiesCloner {

    /**
     * Метод, быстро осуществляющий глубокое клонирование DomainObject. Пока метод clone() не вынесен в интерфейс {@link DomainObject},
     * поддерживается клонирование лишь {@link GenericDomainObject}
     * @param domainObject доменный объект
     * @return клон доменного объекта. на класс возвращаемого конкретного объекта нельзя полагаться, он не обязан быть тем же самым, что передан данному методу
     * @throws ClassCastException если доменный объект не является {@link GenericDomainObject}
     */
    DomainObject fastCloneDomainObject(DomainObject domainObject);

    /**
     * Метод, быстро осуществляющий глубокое клонирование списка DomainObject. Пока метод clone() не вынесен в интерфейс {@link DomainObject},
     * поддерживается клонирование лишь {@link GenericDomainObject}
     * @param domainObjects доменный объект
     * @return клон списка доменных объектов. на класс возвращаемого конкретного объекта и его элементов нельзя полагаться, он не обязан быть тем же самым, что передан данному методу
     * @throws ClassCastException если доменный объект в списке не является {@link GenericDomainObject}
     */
    List<DomainObject> fastCloneDomainObjectList(List<DomainObject> domainObjects);

    /**
     * Метод, быстро осуществляющий глубокое клонирование IdentifiableObjectCollection. Пока метод clone() не вынесен в интерфейс {@link IdentifiableObjectCollection},
     * поддерживается клонирование лишь {@link GenericIdentifiableObjectCollection}
     * @param collection коллекция
     * @return клон коллекции
     * @throws ClassCastException если коллекция не является {@link GenericIdentifiableObjectCollection}
     */
    IdentifiableObjectCollection fastCloneCollection(IdentifiableObjectCollection collection);

    /**
     * Метод, быстро осуществляющий глубокое клонирование {@link Filter}
     * @param filter фильтр
     * @return клон фильтра
     */
    Filter fastCloneFilter(Filter filter);

    /**
     * Метод, быстро осуществляющий глубокое клонирование {@link SortOrder}
     * @param sortOrder фильтр
     * @return клон фильтра
     */
    SortOrder fastCloneSortOrder(SortOrder sortOrder);

    /**
     * Метод, быстро осуществляющий глубокое клонирование списка {@link Value}
     * @param values список {@link Value}
     * @return клон списка  {@link Value}
     */
    List<Value> fastCloneValueList(List<? extends Value> values);

    Id fastCloneId(Id id);
}
