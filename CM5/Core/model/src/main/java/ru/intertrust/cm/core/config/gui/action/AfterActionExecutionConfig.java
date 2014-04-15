package ru.intertrust.cm.core.config.gui.action;

import java.io.Serializable;
import org.simpleframework.xml.Element;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:41.
 */
@Element(name = "after-execution")
public class AfterActionExecutionConfig implements Serializable {

    @Element(name = "on-success-message")
    private MessageConfig messageConfig;

    public MessageConfig getMessageConfig() {
        return messageConfig == null ? new MessageConfig() : messageConfig;
    }
}
