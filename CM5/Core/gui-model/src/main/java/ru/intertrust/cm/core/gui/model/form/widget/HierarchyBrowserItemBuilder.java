package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

public class HierarchyBrowserItemBuilder {
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

    public HierarchyBrowserItemBuilder() {
    }

    public HierarchyBrowserItemBuilder setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
        return this;
    }

    public HierarchyBrowserItemBuilder setId(Id id) {
        this.id = id;
        return this;
    }

    public HierarchyBrowserItemBuilder setNodeCollectionName(String nodeCollectionName) {
        this.nodeCollectionName = nodeCollectionName;
        return this;
    }

    public HierarchyBrowserItemBuilder setChosen(boolean chosen) {
        this.chosen = chosen;
        return this;
    }

    public HierarchyBrowserItemBuilder setMayHaveChildren(boolean mayHaveChildren) {
        this.mayHaveChildren = mayHaveChildren;
        return this;
    }

    public HierarchyBrowserItemBuilder setSingleChoice(Boolean singleChoice) {
        this.singleChoice = singleChoice;
        return this;
    }

    public HierarchyBrowserItemBuilder setDisplayAsHyperlinks(Boolean displayAsHyperlinks) {
        this.displayAsHyperlinks = displayAsHyperlinks;
        return this;
    }

    public HierarchyBrowserItemBuilder setPopupTitle(String popupTitle) {
        this.popupTitle = popupTitle;
        return this;
    }

    public HierarchyBrowserItemBuilder setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
        return this;
    }

    public HierarchyBrowserItemBuilder setSelective(boolean selective) {
        this.selective = selective;
        return this;
    }

    public HierarchyBrowserItem createHierarchyBrowserItem() {
        return new HierarchyBrowserItem(stringRepresentation, id, nodeCollectionName, domainObjectType, chosen, mayHaveChildren,
                singleChoice, displayAsHyperlinks, popupTitle, selective);
    }
}