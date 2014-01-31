package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 11:40
 */
@Root(name = "table-browser")
public class TableBrowserConfig extends WidgetConfig {
    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

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

    @Element(name = "sort-criteria", required = false)
    private SortCriteriaConfig sortCriteriaConfig;

    @Element(name = "dialog-window", required = false)
    private DialogWindowConfig dialogWindowConfig;

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

    public SortCriteriaConfig getSortCriteriaConfig() {
        return sortCriteriaConfig;
    }

    public void setSortCriteriaConfig(SortCriteriaConfig sortCriteriaConfig) {
        this.sortCriteriaConfig = sortCriteriaConfig;
    }

    public DialogWindowConfig getDialogWindowConfig() {
        return dialogWindowConfig;
    }

    public void setDialogWindowConfig(DialogWindowConfig dialogWindowConfig) {
        this.dialogWindowConfig = dialogWindowConfig;
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

        if (sortCriteriaConfig != null ? !sortCriteriaConfig.equals(that.sortCriteriaConfig) :
                that.sortCriteriaConfig != null) {
            return false;
        }
        if (dialogWindowConfig != null ? !dialogWindowConfig.equals(that.dialogWindowConfig) :
                that.dialogWindowConfig != null) {
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
        result = 31 * result + (sortCriteriaConfig != null ? sortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (dialogWindowConfig != null ? dialogWindowConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "table-browser";
    }
}
