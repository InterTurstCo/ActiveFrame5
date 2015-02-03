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
    private String domainObjectType;
    private boolean selective;
    private Id parentId;

    public HierarchyBrowserItem() {
    }

    public HierarchyBrowserItem(String stringRepresentation, Id id, String nodeCollectionName, String domainObjectType,
                                boolean chosen, boolean mayHaveChildren, Boolean singleChoice,
                                Boolean displayAsHyperlinks, String popupTitle, boolean selective) {
        this.stringRepresentation = stringRepresentation;
        this.id = id;
        this.nodeCollectionName = nodeCollectionName;
        this.domainObjectType = domainObjectType;
        this.chosen = chosen;
        this.mayHaveChildren = mayHaveChildren;
        this.singleChoice = singleChoice;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.popupTitle = popupTitle;
        this.selective = selective;
    }
    public HierarchyBrowserItem(String stringRepresentation, Id id, String nodeCollectionName, String domainObjectType,
                                boolean chosen, boolean mayHaveChildren, Boolean singleChoice,
                                Boolean displayAsHyperlinks, String popupTitle, boolean selective, Id parentId) {
        this(stringRepresentation, id, nodeCollectionName, domainObjectType, chosen, mayHaveChildren, singleChoice,
                displayAsHyperlinks, popupTitle, selective);
        this.parentId = parentId;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;

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

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public boolean isSelective() {
        return selective;
    }

    public void setSelective(boolean selective) {
        this.selective = selective;
    }

    public Id getParentId() {
        return parentId;
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

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 31 + (id != null ? id.hashCode() : 0);

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
                .setDomainObjectType(domainObjectType)
                .setSelective(selective)
                .setParentId(parentId)
                .createHierarchyBrowserItem();
        return result;

    }
}
