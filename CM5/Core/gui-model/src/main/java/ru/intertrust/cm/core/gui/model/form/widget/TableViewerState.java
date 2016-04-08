package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.tableviewer.TableViewerConfig;

import java.util.Collection;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 21:00
 */
public class TableViewerState extends WidgetState {
    private TableViewerConfig tableViewerConfig;
    private CreatedObjectsConfig restrictedCreatedObjectsConfig;
    private Map<String, Collection<String>> parentWidgetIdsForNewFormMap;
    private DomainObject rootObject;

    public TableViewerState() {
    }

    public TableViewerState(TableViewerConfig tableViewerConfig) {
        this.tableViewerConfig = tableViewerConfig;
    }

    public TableViewerConfig getTableViewerConfig() {
        return tableViewerConfig;
    }

    public CreatedObjectsConfig getRestrictedCreatedObjectsConfig() {
        return restrictedCreatedObjectsConfig;
    }

    public void setRestrictedCreatedObjectsConfig(CreatedObjectsConfig restrictedCreatedObjectsConfig) {
        this.restrictedCreatedObjectsConfig = restrictedCreatedObjectsConfig;
    }

    public boolean hasAllowedCreationDoTypes(){
        return !restrictedCreatedObjectsConfig.getCreateObjectConfigs().isEmpty();
    }

    public Map<String, Collection<String>> getParentWidgetIdsForNewFormMap() {
        return parentWidgetIdsForNewFormMap;
    }

    public void setParentWidgetIdsForNewFormMap(Map<String, Collection<String>> parentWidgetIdsForNewFormMap) {
        this.parentWidgetIdsForNewFormMap = parentWidgetIdsForNewFormMap;
    }

    public DomainObject getRootObject() {
        return rootObject;
    }

    public void setRootObject(DomainObject rootObject) {
        this.rootObject = rootObject;
    }
}
