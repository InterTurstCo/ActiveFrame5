package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:21
 */
public class ActionContext implements Dto {
    private Id rootObjectId;

    private AbstractActionConfig actionConfig;

    private FormState confirmFormState;

    private List<ActionContext> innerContexts;

    private List<Id> objectsIds;
    /**
     * Default constructor.
     */
    public ActionContext(){}

    public ActionContext(final AbstractActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }

    public Id getRootObjectId() {
        return rootObjectId;
    }

    public void setRootObjectId(Id rootObjectId) {
        this.rootObjectId = rootObjectId;
    }

    public <T extends AbstractActionConfig> T getActionConfig() {
        return (T) actionConfig;
    }

    public void setActionConfig(AbstractActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }

    public FormState getConfirmFormState() {
        return confirmFormState;
    }

    public void setConfirmFormState(FormState confirmFormState) {
        this.confirmFormState = confirmFormState;
    }

    public List<ActionContext> getInnerContexts() {
        if(innerContexts==null)
            innerContexts = new ArrayList<>();
        return innerContexts;
    }

    public List<Id> getObjectsIds() {
        if(objectsIds == null){
            objectsIds = new ArrayList<>();
        }
        return objectsIds;
    }

    public void setObjectsIds(List<Id> objectsIds) {
        this.objectsIds = objectsIds;
    }

    public void setInnerContexts(List<ActionContext> innerContexts) {
        this.innerContexts = innerContexts;
    }
}
