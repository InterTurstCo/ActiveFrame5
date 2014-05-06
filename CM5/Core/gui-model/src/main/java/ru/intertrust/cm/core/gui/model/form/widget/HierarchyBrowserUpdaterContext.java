package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FillParentOnAddConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.04.14
 *         Time: 11:15
 */
public class HierarchyBrowserUpdaterContext implements Dto {
    private FillParentOnAddConfig fillParentOnAddConfig;
    private Id idToReferWith;

    public HierarchyBrowserUpdaterContext() {
    }

    public HierarchyBrowserUpdaterContext(FillParentOnAddConfig fillParentOnAddConfig, Id idToReferWith) {
        this.fillParentOnAddConfig = fillParentOnAddConfig;
        this.idToReferWith = idToReferWith;
    }

    public FillParentOnAddConfig getFillParentOnAddConfig() {
        return fillParentOnAddConfig;
    }

    public void setFillParentOnAddConfig(FillParentOnAddConfig fillParentOnAddConfig) {
        this.fillParentOnAddConfig = fillParentOnAddConfig;
    }

    public Id getIdToReferWith() {
        return idToReferWith;
    }

    public void setIdToReferWith(Id idToReferWith) {
        this.idToReferWith = idToReferWith;
    }
}
