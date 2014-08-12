package ru.intertrust.cm.core.gui.model.action.system;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 17:32.
 */
public class CollectionColumnOrderActionContext extends AbstractUserSettingActionContext {
    public static final String COMPONENT_NAME = "collection.column.order.action";

    private List<String> orders = new ArrayList<>();

    public List<String> getOrders() {
        return orders;
    }

    public CollectionColumnOrderActionContext addOrder(final String order) {
        orders.add(order);
        return this;
    }
}
