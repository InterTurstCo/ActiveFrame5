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
}
