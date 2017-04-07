package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "suggest-box", strict = false)
public class SuggestBoxConfig extends LinkEditingWidgetConfig implements Dto {
    @NotNullLogicalValidation
    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @NotNullLogicalValidation
    @Element(name = "drop-down-pattern", required = false)
    private DropdownPatternConfig dropdownPatternConfig;

    @NotNullLogicalValidation
    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

    @NotNullLogicalValidation
    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "page-size",required = false)
    private Integer pageSize;

    @Element(name = "selection-style",required = false)
    private SelectionStyleConfig selectionStyleConfig;

    @Transient
    @Deprecated
    Integer maxDropDownWidth;

    @Transient
    @Deprecated
    Integer maxDropDownHeight;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    @Element(name = "clear-all-button", required = false)
    private ClearAllButtonConfig clearAllButtonConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    @Element(name = "display-values-as-links", required = false)
    private DisplayValuesAsLinksConfig displayValuesAsLinksConfig;

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    @Element(name = "selection-filters", required = false)
    private SelectionFiltersConfig selectionFiltersConfig;

    @Element(name = "selection-sort-criteria",required = false)
    private SelectionSortCriteriaConfig selectionSortCriteriaConfig;

    @Element(name = "linked-form", required = false)
    private LinkedFormConfig linkedFormConfig;

    @Element(name = "collection-extra-filters",required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;
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
        return pageSize == null ? 20 : pageSize;
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

    public DisplayValuesAsLinksConfig getDisplayValuesAsLinksConfig() {
        return displayValuesAsLinksConfig;
    }

    public void setDisplayValuesAsLinksConfig(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        this.displayValuesAsLinksConfig = displayValuesAsLinksConfig;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    public void setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
    }

    public SelectionSortCriteriaConfig getSelectionSortCriteriaConfig() {
        return selectionSortCriteriaConfig;
    }

    public void setSelectionSortCriteriaConfig(SelectionSortCriteriaConfig selectionSortCriteriaConfig) {
        this.selectionSortCriteriaConfig = selectionSortCriteriaConfig;
    }

    public LinkedFormConfig getLinkedFormConfig() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(LinkedFormConfig linkedFormConfig) {
        this.linkedFormConfig = linkedFormConfig;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
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

        if (displayValuesAsLinksConfig != null ? !displayValuesAsLinksConfig.equals(that.displayValuesAsLinksConfig) :
                that.displayValuesAsLinksConfig != null) {
            return false;
        }
        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.formattingConfig != null) {
            return false;
        }
        if (selectionFiltersConfig != null ? !selectionFiltersConfig.equals(that.selectionFiltersConfig) :
                that.selectionFiltersConfig != null) {
            return false;
        }
        if (selectionSortCriteriaConfig != null ? !selectionSortCriteriaConfig.equals(that.selectionSortCriteriaConfig) :
                that.selectionSortCriteriaConfig != null) {
            return false;
        }
        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null) {
            return false;
        }
        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig)
                : that.collectionExtraFiltersConfig != null) {
            return false;
        }

        return true;
    }
}


