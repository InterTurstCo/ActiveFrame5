package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;

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

    @Element(name = "selection-style",required = false)
    SelectionStyleConfig selectionStyleConfig;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

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

    public SelectionStyleConfig getSelectionStyleConfig() {
        return selectionStyleConfig;
    }

    public void setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
    }

    public SingleChoiceConfig getSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(SingleChoiceConfig singleChoice) {
        this.singleChoice = singleChoice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SuggestBoxConfig that = (SuggestBoxConfig) o;

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) : that.
                collectionRefConfig != null) {
            return false;
        }
        if (dropdownPatternConfig != null ? !dropdownPatternConfig.equals(that.dropdownPatternConfig) : that.
                dropdownPatternConfig != null) {
            return false;
        }
        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.inputTextFilterConfig) : that.
                inputTextFilterConfig != null) {
            return false;
        }
        if (pageSize != null ? !pageSize.equals(that.pageSize) : that.pageSize != null) {
            return false;
        }
        if (selectionExcludeFilterConfig != null ? !selectionExcludeFilterConfig.
                equals(that.selectionExcludeFilterConfig) : that.selectionExcludeFilterConfig != null) {
            return false;
        }
        if (selectionFilterConfig != null ? !selectionFilterConfig.equals(that.selectionFilterConfig) : that.
                selectionFilterConfig != null) {
            return false;
        }
        if (selectionPatternConfig != null ? !selectionPatternConfig.equals(that.selectionPatternConfig) : that.
                selectionPatternConfig != null) {
            return false;
        }
        if (selectionStyleConfig != null ? !selectionStyleConfig.equals(that.selectionStyleConfig) : that.
                selectionStyleConfig != null) {
            return false;
        }
        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) : that.singleChoice != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (selectionFilterConfig != null ? selectionFilterConfig.hashCode() : 0);
        result = 31 * result + (selectionExcludeFilterConfig != null ? selectionExcludeFilterConfig.hashCode() : 0);
        result = 31 * result + (dropdownPatternConfig != null ? dropdownPatternConfig.hashCode() : 0);
        result = 31 * result + (selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0);
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0);
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        return result;
    }
}


