package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.CollectionTableButtonsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 11:40
 */
@Root(name = "table-browser")
public class TableBrowserConfig extends LinkEditingWidgetConfig {
    @Attribute(name = "resizable", required = false)
    private boolean resizable;

    @NotNullLogicalValidation
    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @NotNullLogicalValidation
    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @NotNullLogicalValidation
    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

    @NotNullLogicalValidation
    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "page-size", required = false)
    private Integer pageSize;

    @Element(name = "display-chosen-values", required = false)
    private DisplayChosenValuesConfig displayChosenValues;

    @Element(name = "selection-style", required = false)
    private SelectionStyleConfig selectionStyleConfig;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    @Element(name = "clear-all-button", required = false)
    private ClearAllButtonConfig clearAllButtonConfig;

    @Element(name = "add-button", required = false)
    private AddButtonConfig addButtonConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    @Element(name = "dialog-window", required = false)
    private DialogWindowConfig dialogWindowConfig;

    @Element(name = "display-values-as-links", required = false)
    private DisplayValuesAsLinksConfig displayValuesAsLinksConfig;

    @Element(name = "initial-filters", required = false)
    private InitialFiltersConfig initialFiltersConfig;

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    @Element(name = "selection-filters", required = false)
    private SelectionFiltersConfig selectionFiltersConfig;

    @Element(name = "selection-sort-criteria",required = false)
    private SelectionSortCriteriaConfig selectionSortCriteriaConfig;

    @Element(name = "linked-form",required = false)
    private LinkedFormConfig linkedFormConfig;

    @Element(name = "collection-extra-filters",required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;

    @Element(name = "collection-table-buttons", required = false)
    private CollectionTableButtonsConfig collectionTableButtonsConfig;

    public CollectionViewRefConfig getCollectionViewRefConfig() {
        return collectionViewRefConfig;
    }

    public void setCollectionViewRefConfig(CollectionViewRefConfig collectionViewRefConfig) {
        this.collectionViewRefConfig = collectionViewRefConfig;
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
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

    public DisplayChosenValuesConfig getDisplayChosenValues() {
        return displayChosenValues;
    }

    public void setDisplayChosenValues(DisplayChosenValuesConfig displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
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

    public AddButtonConfig getAddButtonConfig() {
        return addButtonConfig;
    }

    public void setAddButtonConfig(AddButtonConfig addButtonConfig) {
        this.addButtonConfig = addButtonConfig;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public DialogWindowConfig getDialogWindowConfig() {
        return dialogWindowConfig;
    }

    public void setDialogWindowConfig(DialogWindowConfig dialogWindowConfig) {
        this.dialogWindowConfig = dialogWindowConfig;
    }

    public DisplayValuesAsLinksConfig getDisplayValuesAsLinksConfig() {
        return displayValuesAsLinksConfig;
    }

    public void setDisplayValuesAsLinksConfig(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        this.displayValuesAsLinksConfig = displayValuesAsLinksConfig;
    }

    public InitialFiltersConfig getInitialFiltersConfig() {
        return initialFiltersConfig;
    }

    public void setInitialFiltersConfig(InitialFiltersConfig initialFiltersConfig) {
        this.initialFiltersConfig = initialFiltersConfig;
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

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public CollectionTableButtonsConfig getCollectionTableButtonsConfig() {
        return collectionTableButtonsConfig;
    }

    public void setCollectionTableButtonsConfig(CollectionTableButtonsConfig collectionTableButtonsConfig) {
        this.collectionTableButtonsConfig = collectionTableButtonsConfig;
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

        TableBrowserConfig that = (TableBrowserConfig) o;

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) :
                that.collectionRefConfig != null) {
            return false;
        }
        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.collectionViewRefConfig) :
                that.collectionViewRefConfig != null) {
            return false;
        }
        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.inputTextFilterConfig) :
                that.inputTextFilterConfig != null) {
            return false;
        }
        if (pageSize != null ? !pageSize.equals(that.pageSize) : that.pageSize != null) {
            return false;
        }

        if (selectionPatternConfig != null ? !selectionPatternConfig.equals(that.selectionPatternConfig) :
                that.selectionPatternConfig != null) {
            return false;
        }
        if (displayChosenValues != null ? !displayChosenValues.equals(that.displayChosenValues) :
                that.displayChosenValues != null) {
            return false;
        }

        if (selectionStyleConfig != null ? !selectionStyleConfig.equals(that.selectionStyleConfig) :
                that.selectionPatternConfig != null) {
            return false;
        }
        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) : that.singleChoice != null) {
            return false;
        }

        if (clearAllButtonConfig != null ? !clearAllButtonConfig.equals(that.clearAllButtonConfig) :
                that.clearAllButtonConfig != null) {
            return false;
        }

        if (addButtonConfig != null ? !addButtonConfig.equals(that.addButtonConfig) : that.addButtonConfig != null) {
            return false;
        }

        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) :
                that.defaultSortCriteriaConfig != null) {
            return false;
        }
        if (dialogWindowConfig != null ? !dialogWindowConfig.equals(that.dialogWindowConfig) :
                that.dialogWindowConfig != null) {
            return false;
        }

        if (displayValuesAsLinksConfig!= null ? !displayValuesAsLinksConfig.equals(that.displayValuesAsLinksConfig) :
                that.displayValuesAsLinksConfig!= null) {
            return false;
        }
        if (initialFiltersConfig != null ? !initialFiltersConfig.equals(that.initialFiltersConfig) : that.initialFiltersConfig != null) {
            return false;
        }
        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.formattingConfig != null) {
            return false;
        }
        if (selectionFiltersConfig != null ? !selectionFiltersConfig.equals(that.selectionFiltersConfig)
                : that.selectionFiltersConfig != null) {
            return false;
        }
        if (selectionSortCriteriaConfig != null ? !selectionSortCriteriaConfig.equals(that.selectionSortCriteriaConfig)
                : that.selectionSortCriteriaConfig != null) {
            return false;
        }
        if (linkedFormConfig != null ? !linkedFormConfig.equals(that.linkedFormConfig) : that.linkedFormConfig != null) {
            return false;
        }
        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig)
                : that.collectionExtraFiltersConfig != null) {
            return false;
        }
        if (resizable != that.resizable) {
            return false;
        }
        if (collectionTableButtonsConfig != null ? !collectionTableButtonsConfig.equals(that.collectionTableButtonsConfig)
                : that.collectionTableButtonsConfig != null) {
            return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0);
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (displayChosenValues != null ? displayChosenValues.hashCode() : 0);
        result = 31 * result + (selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0);
        result = 31 * result + (clearAllButtonConfig != null ? clearAllButtonConfig.hashCode() : 0);
        result = 31 * result + (addButtonConfig != null ? addButtonConfig.hashCode() : 0);
        result = 31 * result + (dialogWindowConfig != null ? dialogWindowConfig.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (displayValuesAsLinksConfig != null ? displayValuesAsLinksConfig.hashCode() : 0);
        result = 31 * result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        result = 31 * result + (initialFiltersConfig != null ? initialFiltersConfig.hashCode() : 0);
        result = 31 * result + (selectionFiltersConfig != null ? selectionFiltersConfig.hashCode() : 0);
        result = 31 * result + (selectionSortCriteriaConfig != null ? selectionSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (linkedFormConfig != null ? linkedFormConfig.hashCode() : 0);
        result = 31 * result + (collectionExtraFiltersConfig != null ? collectionExtraFiltersConfig.hashCode() : 0);
        result = 31 * result + (resizable ? 1 : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "table-browser";
    }
}
