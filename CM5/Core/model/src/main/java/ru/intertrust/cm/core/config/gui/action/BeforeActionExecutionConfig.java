package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 17:22.
 */
@Element(name = "before-execution")
public class BeforeActionExecutionConfig implements Dto {

    @Element(name = "confirmation-message", required = false)
    private MessageConfig messageConfig;

    @Element(name = "linked-domain-object", required = false)
    private LinkedDomainObjectConfig linkedDomainObjectConfig;

    @Element(name = "save-context", required = false)
    private BooleanValueConfig saveContext;

    public MessageConfig getMessageConfig() {
        return messageConfig == null ? new MessageConfig() : messageConfig;
    }

    public boolean isSaveContext() {
        return saveContext == null ? true : saveContext.getValue();
    }

    public LinkedDomainObjectConfig getLinkedDomainObjectConfig() {
        return linkedDomainObjectConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeforeActionExecutionConfig that = (BeforeActionExecutionConfig) o;

        if (messageConfig != null ? !messageConfig.equals(that.messageConfig) : that.messageConfig != null)
            return false;
        if (linkedDomainObjectConfig != null ? !linkedDomainObjectConfig.equals(that.linkedDomainObjectConfig) : that.linkedDomainObjectConfig != null)
            return false;
        if (saveContext != null ? !saveContext.equals(that.saveContext) : that.saveContext != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return messageConfig != null ? messageConfig.hashCode() : 0;
    }
}
