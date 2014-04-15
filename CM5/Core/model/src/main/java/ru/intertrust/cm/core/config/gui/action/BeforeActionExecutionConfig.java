package ru.intertrust.cm.core.config.gui.action;

import java.io.Serializable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 17:22.
 */
@Element(name = "before-execution")
public class BeforeActionExecutionConfig implements Serializable {

    @Element(name = "confirmation-message")
    private MessageConfig messageConfig;

    public MessageConfig getMessageConfig() {
        return messageConfig == null ? new MessageConfig() : messageConfig;
    }
}
