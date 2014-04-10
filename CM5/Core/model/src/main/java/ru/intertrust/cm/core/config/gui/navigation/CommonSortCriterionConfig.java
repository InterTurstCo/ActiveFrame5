package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 20/9/13
 *         Time: 12:05 PM
 */
public class CommonSortCriterionConfig implements Dto {
    private static final String DESCENDING = "desc";

    @Attribute(name = "order", required = false)
    private String order;

    public SortCriterion.Order getOrder() {

        if (DESCENDING.equalsIgnoreCase(order)) {
            return SortCriterion.Order.DESCENDING;
        }

        return SortCriterion.Order.ASCENDING;
    }

    public String getOrderString(){
        return order;
    }

    public void setOrderString(String order) {
        this.order = order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommonSortCriterionConfig that = (CommonSortCriterionConfig) o;

        if (order != null ? !order.equals(that.getOrder()) : that.getOrder() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return  31 * (order != null ? order.hashCode() : 0);

    }
}
