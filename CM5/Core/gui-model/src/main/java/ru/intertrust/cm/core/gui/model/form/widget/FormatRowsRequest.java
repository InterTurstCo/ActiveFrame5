package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.12.13
 *         Time: 13:15
 */
public class FormatRowsRequest implements Dto {
    private String selectionPattern;
    private ArrayList<Id> idsShouldBeFormatted = new ArrayList<Id>();
    private String collectionName;
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;
    public String getSelectionPattern() {
        return selectionPattern;
    }

    public void setSelectionPattern(String selectionPattern) {
        this.selectionPattern = selectionPattern;
    }

    public ArrayList<Id> getIdsShouldBeFormatted() {
        return idsShouldBeFormatted;
    }

    public void setIdsShouldBeFormatted(ArrayList<Id> idsShouldBeFormatted) {
        this.idsShouldBeFormatted = idsShouldBeFormatted;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }
}
