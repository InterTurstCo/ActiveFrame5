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

    @Element(name = "perform-validation", required = false)
    private BooleanValueConfig performValidation;

    @Element(name = "domain-object-to-create", required = false)
    private DomainObjectToCreateConfig domainObjectToCreateConfig;

    @Element(name = "save-context", required = false)
    private BooleanValueConfig saveContext;

    public MessageConfig getMessageConfig() {
        return messageConfig == null ? new MessageConfig() : messageConfig;
    }

    public boolean isPerformValidation() {
        return performValidation == null ? true : performValidation.getValue();
    }

    public boolean isSaveContext() {
        return saveContext == null ? true : saveContext.getValue();
    }

    public DomainObjectToCreateConfig getDomainObjectToCreateConfig() {
        return domainObjectToCreateConfig;
    }
}
