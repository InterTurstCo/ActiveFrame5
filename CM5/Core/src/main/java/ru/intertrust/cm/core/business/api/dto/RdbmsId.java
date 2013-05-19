package ru.intertrust.cm.core.business.api.dto;

/**
 * Уникальный идентификатор бизнес-объекта, состоящий из целочисленного ключа и названия бизнес-объекта.<br/>
 * Подобные ключи используются для идентификации объектов в реляционных СУБД.
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:06
 */
public class RdbmsId implements Id {
    private String name;
    private long id;

    /**
     * Создаёт идентификатор бизнес-объекта
     */
    public RdbmsId() {
    }

    /**
     * Создаёт идентификатор бизнес-объекта
     * @param name название бизнес объекта
     * @param id целочисленный идентификатор
     */
    public RdbmsId(String name, long id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Возвращает название бизнес-объекта
     * @return название бизнес-объекта
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название бизнес-объекта
     * @param name название бизнес-объекта
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает целочисленный идентификатор бизнес-объекта
     * @return целочисленный идентификатор бизнес-объекта
     */
    public long getId() {
        return id;
    }

    /**
     * Устанавливает целочисленный идентификатор бизнес-объекта
     * @param id целочисленный идентификатор бизнес-объекта
     */
    public void setId(long id) {
        this.id = id;
    }
}
