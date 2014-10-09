package ru.intertrust.cm.core.business.api.dto.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.10.2014
 *         Time: 6:49
 */
public class PopupTitlesHolder implements Dto {
    private String titleNewObject;
    private String titleExistingObject;

    public String getTitleNewObject() {
        return titleNewObject;
    }

    public void setTitleNewObject(String titleNewObject) {
        this.titleNewObject = titleNewObject;
    }

    public String getTitleExistingObject() {
        return titleExistingObject;
    }

    public void setTitleExistingObject(String titleExistingObject) {
        this.titleExistingObject = titleExistingObject;
    }
}
