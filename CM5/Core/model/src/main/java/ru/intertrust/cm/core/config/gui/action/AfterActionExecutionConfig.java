package ru.intertrust.cm.core.config.gui.action;

import java.io.Serializable;
import org.simpleframework.xml.Element;

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
}
