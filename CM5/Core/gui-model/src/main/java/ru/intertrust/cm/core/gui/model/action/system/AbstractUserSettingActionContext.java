package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 17:33.
 */
public class AbstractUserSettingActionContext extends ActionContext {

    private String collectionName;
    private String collectionViewName;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionViewName() {
        return collectionViewName;
    }

    public void setCollectionViewName(String collectionViewName) {
        this.collectionViewName = collectionViewName;
    }
}
