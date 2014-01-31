package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "suggest-box")
public class SuggestBoxConfig extends WidgetConfig implements Dto {

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "drop-down-pattern", required = false)
    private DropdownPatternConfig dropdownPatternConfig;

    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "page-size",required = false)
    private Integer pageSize;

    @Element(name = "selection-style",required = false)
    private SelectionStyleConfig selectionStyleConfig;

    @Element(name = "max-drop-down-width", required = false)
    Integer maxDropDownWidth;

    @Element(name = "max-drop-down-height", required = false)
    Integer maxDropDownHeight;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    @Element(name = "clear-all-button", required = false)
    private ClearAllButtonConfig clearAllButtonConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

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

    public ClearAllButtonConfig getClearAllButtonConfig() {
        return clearAllButtonConfig;
    }

    public void setClearAllButtonConfig(ClearAllButtonConfig clearAllButtonConfig) {
        this.clearAllButtonConfig = clearAllButtonConfig;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public Integer getMaxDropDownWidth() {
        return maxDropDownWidth;
    }

    public void setMaxDropDownWidth(Integer maxDropDownWidth) {
        this.maxDropDownWidth = maxDropDownWidth;
    }

    public Integer getMaxDropDownHeight() {
        return maxDropDownHeight;
    }

    public void setMaxDropDownHeight(Integer maxDropDownHeight) {
        this.maxDropDownHeight = maxDropDownHeight;
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

        if (clearAllButtonConfig != null ? !clearAllButtonConfig.equals(that.clearAllButtonConfig) :
                that.clearAllButtonConfig != null) {
            return false;
        }

        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) :
                that.defaultSortCriteriaConfig != null) {
            return false;
        }

        if (maxDropDownWidth != null ? !maxDropDownWidth.equals(that.maxDropDownWidth) : that.maxDropDownWidth != null) {
            return false;
        }

        if (maxDropDownHeight != null ? !maxDropDownHeight.equals(that.maxDropDownHeight) : that.maxDropDownHeight != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (dropdownPatternConfig != null ? dropdownPatternConfig.hashCode() : 0);
        result = 31 * result + (selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0);
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0);
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        result = 31 * result + (clearAllButtonConfig != null ? clearAllButtonConfig.hashCode() : 0);
        result = 31 * result + (maxDropDownWidth != null ? maxDropDownWidth.hashCode() : 0);
        result = 31 * result + (maxDropDownHeight != null ? maxDropDownHeight.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        return result;
    }
}


