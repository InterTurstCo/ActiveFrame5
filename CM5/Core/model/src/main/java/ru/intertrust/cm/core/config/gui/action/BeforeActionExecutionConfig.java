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
}
