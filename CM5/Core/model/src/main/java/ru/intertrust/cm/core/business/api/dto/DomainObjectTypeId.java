package ru.intertrust.cm.core.business.api.dto;

/**
 * Dto для пары имя типа доменного объекта + идентификатор типа доменного объекта
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:35 PM
 */
public class DomainObjectTypeId implements Dto {

    private String name;
    private Integer id;

    /**
     * Создает #DomainObjectTypeId
     * @param name имя типа доменного объекта
     * @param id идентификатор типа доменного объекта
     */
    public DomainObjectTypeId(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Возвращает имя типа доменного объекта
     * @return имя типа доменного объекта
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает идентификатор типа доменного объекта
     * @return идентификатор типа доменного объекта
     */
    public Integer getId() {
        return id;
    }
}
