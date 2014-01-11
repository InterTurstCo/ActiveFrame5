package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.ActionConfig;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 11.01.14
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
public class SaveToCSVContext extends ActionContext {
    private int id;
    private String collectionName;



    public SaveToCSVContext() {
    }

    public SaveToCSVContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}