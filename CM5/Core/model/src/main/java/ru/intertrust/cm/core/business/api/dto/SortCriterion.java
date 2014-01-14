package ru.intertrust.cm.core.business.api.dto;

/**
 * Критерий сортировки
 *
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 23:32
 */
public class SortCriterion implements Dto {
    /**
     * Порядок сортировки
     */
    public static enum Order implements Dto {
        /**
         * По возрастанию
         */
        ASCENDING,

        /**
         * По убыванию
         */
        DESCENDING
    }

    private String field;
    private Order order;

    /**
     * Конструктор по умолчанию, создающий пустой порядок сортировки. Требуется исключительно для сериализации и на
     * практике применяться не должен
     * @deprecated
     */
    public SortCriterion() {
    }

    /**
     * Создаёт новый критерий сортировки
     * @param field поле, по которому осуществляется сортировка
     * @param order порядок сортировки
     */
    public SortCriterion(String field, Order order) {
        this.field = field;
        this.order = order;
    }

    /**
     * Возвращает поле, по которому осуществляется сортировка
     * @return поле, по которому осуществляется сортировка
     */
    public String getField() {
        return field;
    }

    /**
     * Возвращает порядок сортирвки
     * @return порядок сортирвки
     */
    public Order getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SortCriterion another = (SortCriterion) o;

        if (!field.equals(another.field)) return false;
        if (order != another.order) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + (order == Order.ASCENDING ? 29 : 17);
        return result;
    }
}
