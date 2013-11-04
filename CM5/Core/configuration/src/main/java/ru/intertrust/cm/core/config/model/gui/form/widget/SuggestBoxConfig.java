package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionRefConfig;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class SuggestBoxConfig extends WidgetConfig implements Dto {

    @Element(name = "collection-ref", required = false)
    CollectionRefConfig collectionRefConfig;

    @Element(name = "selection-filter", required = false)
    SelectionFilterConfig selectionFilterConfig;

    @Element(name = "selection-exclude-filter", required = false)
    SelectionExcludeFilterConfig selectionExcludeFilterConfig;

    @Element(name = "drop-down-pattern", required = false)
    DropdownPatternConfig dropdownPatternConfig;

    @Element(name = "selection-pattern", required = false)
    SelectionPatternConfig selectionPatternConfig;

    @Element(name = "input-text-filter", required = false)
    InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "page-size",required = false)
    Integer pageSize;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String getComponentName() {
        return "suggest-box";
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public SelectionFilterConfig getSelectionFilterConfig() {
        return selectionFilterConfig;
    }

    public void setSelectionFilterConfig(SelectionFilterConfig selectionFilterConfig) {
        this.selectionFilterConfig = selectionFilterConfig;
    }

    public SelectionExcludeFilterConfig getSelectionExcludeFilterConfig() {
        return selectionExcludeFilterConfig;
    }

    public void setSelectionExcludeFilterConfig(SelectionExcludeFilterConfig selectionExcludeFilterConfig) {
        this.selectionExcludeFilterConfig = selectionExcludeFilterConfig;
    }

    public DropdownPatternConfig getDropdownPatternConfig() {
        return dropdownPatternConfig;
    }

    public void setDropdownPatternConfig(DropdownPatternConfig dropdownPatternConfig) {
        this.dropdownPatternConfig = dropdownPatternConfig;
    }

    public SelectionPatternConfig getSelectionPatternConfig() {
        return selectionPatternConfig;
    }

    public void setSelectionPatternConfig(SelectionPatternConfig selectionPatternConfig) {
        this.selectionPatternConfig = selectionPatternConfig;
    }

    public InputTextFilterConfig getInputTextFilterConfig() {
        return inputTextFilterConfig;
    }

    public void setInputTextFilterConfig(InputTextFilterConfig inputTextFilterConfig) {
        this.inputTextFilterConfig = inputTextFilterConfig;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}


