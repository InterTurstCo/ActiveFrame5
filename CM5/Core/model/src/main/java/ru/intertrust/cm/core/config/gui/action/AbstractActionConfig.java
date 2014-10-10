package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.ValidatorsConfig;

import java.util.List;

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

    @Element(name = "validators", required = false)
    private ValidatorsConfig validatorsConfig;

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

    public List<ValidatorConfig> getCustomValidators() {
        if(validatorsConfig != null) {
            return validatorsConfig.getValidators();
        }
        return null;
    }

    public ValidatorsConfig getValidatorsConfig() {
        return validatorsConfig;
    }

    @Override
    public int compareTo(AbstractActionConfig obj) {
        return getOrder() - obj.getOrder();
    }

}
