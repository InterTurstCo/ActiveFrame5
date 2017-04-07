package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:41.
 */
@Element(name = "after-execution")
public class AfterActionExecutionConfig implements Serializable {

    @Element(name = "on-success-message", required = false)
    private OnSuccessMessageConfig successMessageConfig;

    public OnSuccessMessageConfig getMessageConfig() {
        return successMessageConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AfterActionExecutionConfig that = (AfterActionExecutionConfig) o;

        if (successMessageConfig != null ? !successMessageConfig.equals(that.successMessageConfig) : that.successMessageConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return successMessageConfig != null ? successMessageConfig.hashCode() : 0;
    }
}
