package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserItem implements Dto {

    private String stringRepresentation;
    private Id id;
    private String nodeCollectionName;
    private boolean chosen;
    private boolean mayHaveChildren;
    private Boolean singleChoice;
    private Boolean displayAsHyperlinks;
    private String popupTitle;

    public HierarchyBrowserItem() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;

    }

    public HierarchyBrowserItem(String stringRepresentation, Id id, String nodeCollectionName,
                                boolean chosen, boolean mayHaveChildren, Boolean singleChoice,
                                Boolean displayAsHyperlinks, String popupTitle) {
        this.stringRepresentation = stringRepresentation;
        this.id = id;
        this.nodeCollectionName = nodeCollectionName;
        this.chosen = chosen;
        this.mayHaveChildren = mayHaveChildren;
        this.singleChoice = singleChoice;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.popupTitle = popupTitle;
    }

    public boolean isMayHaveChildren() {
        return mayHaveChildren;
    }

    public void setMayHaveChildren(boolean mayHaveChildren) {
        this.mayHaveChildren = mayHaveChildren;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String getNodeCollectionName() {
        return nodeCollectionName;
    }

    public void setNodeCollectionName(String nodeCollectionName) {
        this.nodeCollectionName = nodeCollectionName;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    public Boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(Boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public Boolean isDisplayAsHyperlinks() {
        return displayAsHyperlinks;
    }

    public void setDisplayAsHyperlinks(boolean displayAsHyperlinks) {
        this.displayAsHyperlinks = displayAsHyperlinks;
    }

    public String getPopupTitle() {
        return popupTitle;
    }

    public void setPopupTitle(String popupTitle) {
        this.popupTitle = popupTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HierarchyBrowserItem)) {
            return false;
        }

        HierarchyBrowserItem that = (HierarchyBrowserItem) o;

        if (popupTitle != null ? !popupTitle.equals(that.popupTitle) : that.popupTitle != null) {
            return false;
        }
        if (mayHaveChildren != that.mayHaveChildren) {
            return false;
        }
        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) : that.singleChoice != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (nodeCollectionName != null ? !nodeCollectionName.equals(that.nodeCollectionName)
                : that.nodeCollectionName != null) {
            return false;
        }
        if (displayAsHyperlinks != null ? !displayAsHyperlinks.equals(that.displayAsHyperlinks)
                : that.displayAsHyperlinks != null) {
            return false;
        }
        if (stringRepresentation != null ? !stringRepresentation.equals(that.stringRepresentation)
                : that.stringRepresentation != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = stringRepresentation != null ? stringRepresentation.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (nodeCollectionName != null ? nodeCollectionName.hashCode() : 0);
        result = 31 * result + (chosen ? 1 : 0);
        result = 31 * result + (mayHaveChildren ? 1 : 0);
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        result = 31 * result + (displayAsHyperlinks != null ? displayAsHyperlinks.hashCode() : 0);
        result = 31 * result + (popupTitle != null ? popupTitle.hashCode() : 0);
        return result;
    }

    public HierarchyBrowserItem getCopy() {
        HierarchyBrowserItem result = new HierarchyBrowserItemBuilder()
                .setStringRepresentation(stringRepresentation)
                .setId(id)
                .setNodeCollectionName(nodeCollectionName)
                .setChosen(chosen)
                .setMayHaveChildren(mayHaveChildren)
                .setPopupTitle(popupTitle)
                .setSingleChoice(singleChoice)
                .setDisplayAsHyperlinks(displayAsHyperlinks)
                .createHierarchyBrowserItem();
        return result;

    }
}
