package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Кэш идентификаторов типов доменных объектов
 * @author vmatsukevich
 *         Date: 9/18/13
 *         Time: 1:40 PM
 */
public interface DomainObjectTypeIdCache {

    /**
     * Возвращает идентификатор типа доменного объекта по его имени
     * @param name имя типа доменного объекта
     * @return идентификатор типа доменного объекта
     */
    public Integer getId(String name);

    /**
     * Возвращает название типа доменного объекта по его идентификатору
     * @param id идентификатор типа доменного объекта
     * @return название типа доменного объекта
     */
    public String getName(Integer id);

    /**
     * Возвращает название типа доменного объекта по идентификатору доменного объекта
     * @param id доменного объекта
     * @return название типа доменного объекта
     */
    public String getName(Id id);
}
