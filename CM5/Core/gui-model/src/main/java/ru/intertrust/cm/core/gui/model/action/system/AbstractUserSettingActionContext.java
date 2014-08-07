package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 17:33.
 */
public class AbstractUserSettingActionContext extends ActionContext {

    private String link;
    private String collectionViewName;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCollectionViewName() {
        return collectionViewName;
    }

    public void setCollectionViewName(String collectionViewName) {
        this.collectionViewName = collectionViewName;
    }
}
