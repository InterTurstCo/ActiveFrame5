package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;

/**
 * Marker of action config.
 * @author Sergey.Okolot
 *         Created on 15.04.2014 11:57.
 */
public class AbstractActionConfig extends BaseAttributeConfig
        implements Comparable<AbstractActionConfig> {
    // @defaultUID
    private static final long serialVersionUID = 1L;

    @Attribute(name = "order", required = false)
    private Integer order;

    public int getOrder() {
        return order == null ? Integer.MAX_VALUE : order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public boolean isMerged() {
        return false;
    }

    public int getWeight() {
        return 0;
    }

    @Override
    public int compareTo(AbstractActionConfig obj) {
        return getOrder() - obj.getOrder();
    }
}
