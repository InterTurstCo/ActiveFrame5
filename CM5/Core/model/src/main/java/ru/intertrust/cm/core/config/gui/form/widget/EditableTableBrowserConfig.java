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
 * Created by Ravil on 26.09.2017.
 */
@Root(name = "editable-table-browser")
public class EditableTableBrowserConfig extends LinkEditingWidgetConfig {
    @Attribute(name = "resizable", required = false)
    private boolean resizable;

    @Attribute(name = "select-btn-enabled", required = false)
    private boolean selectBtnEnabled = true;

    @Attribute(name = "default-btn-enabled", required = false)
    private boolean defaultBtnEnabled = true;

    @Attribute(name = "enter-key-allowed", required = false)
    private boolean enterKeyAllowed = false;

    @Attribute(name = "default-component-name", required = false)
    private String defaultComponentName;

    @Attribute(name = "depend-on-widgets-ids", required = false)
    private String dependOnWidgetsIds;

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

    @Element(name = "selection-sort-criteria", required = false)
    private SelectionSortCriteriaConfig selectionSortCriteriaConfig;

    @Element(name = "collection-extra-filters", required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;

    @Element(name = "collection-table-buttons", required = false)
    private CollectionTableButtonsConfig collectionTableButtonsConfig;

    @Element(name = "select-button", required = true)
    private SelectButtonConfig selectButtonConfig;

    @Element(name = "default-button", required = true)
    private DefaultButtonConfig defaultButtonConfig;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    @Override
    @Deprecated
    public LinkedFormConfig getLinkedFormConfig() {
        return null;
    }

    @Override
    public SelectionStyleConfig getSelectionStyleConfig() {
        return selectionStyleConfig;
    }

    @Override
    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    @Override
    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    @Override
    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    @Override
    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    @Override
    public SelectionPatternConfig getSelectionPatternConfig() {
        return selectionPatternConfig;
    }

    @Override
    public SelectionSortCriteriaConfig getSelectionSortCriteriaConfig() {
        return selectionSortCriteriaConfig;
    }

    public boolean isResizable() {
        return resizable;
    }

    public CollectionViewRefConfig getCollectionViewRefConfig() {
        return collectionViewRefConfig;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public DisplayChosenValuesConfig getDisplayChosenValues() {
        return displayChosenValues;
    }

    public DialogWindowConfig getDialogWindowConfig() {
        return dialogWindowConfig;
    }

    public DisplayValuesAsLinksConfig getDisplayValuesAsLinksConfig() {
        return displayValuesAsLinksConfig;
    }

    public InitialFiltersConfig getInitialFiltersConfig() {
        return initialFiltersConfig;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public CollectionTableButtonsConfig getCollectionTableButtonsConfig() {
        return collectionTableButtonsConfig;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public void setCollectionViewRefConfig(CollectionViewRefConfig collectionViewRefConfig) {
        this.collectionViewRefConfig = collectionViewRefConfig;
    }

    public String getDependOnWidgetsIds() {
        return dependOnWidgetsIds;
    }

    public void setDependOnWidgetsIds(String dependOnWidgetsIds) {
        this.dependOnWidgetsIds = dependOnWidgetsIds;
    }

    public SingleChoiceConfig getSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(SingleChoiceConfig singleChoice) {
        this.singleChoice = singleChoice;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public InputTextFilterConfig getInputTextFilterConfig() {
        return inputTextFilterConfig;
    }

    public void setInputTextFilterConfig(InputTextFilterConfig inputTextFilterConfig) {
        this.inputTextFilterConfig = inputTextFilterConfig;
    }

    public void setSelectionPatternConfig(SelectionPatternConfig selectionPatternConfig) {
        this.selectionPatternConfig = selectionPatternConfig;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setDisplayChosenValues(DisplayChosenValuesConfig displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
    }

    public void setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public void setDialogWindowConfig(DialogWindowConfig dialogWindowConfig) {
        this.dialogWindowConfig = dialogWindowConfig;
    }

    public void setDisplayValuesAsLinksConfig(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        this.displayValuesAsLinksConfig = displayValuesAsLinksConfig;
    }

    public void setInitialFiltersConfig(InitialFiltersConfig initialFiltersConfig) {
        this.initialFiltersConfig = initialFiltersConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public void setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
    }

    public void setSelectionSortCriteriaConfig(SelectionSortCriteriaConfig selectionSortCriteriaConfig) {
        this.selectionSortCriteriaConfig = selectionSortCriteriaConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
    }

    public void setCollectionTableButtonsConfig(CollectionTableButtonsConfig collectionTableButtonsConfig) {
        this.collectionTableButtonsConfig = collectionTableButtonsConfig;
    }

    public boolean isSelectBtnEnabled() {
        return selectBtnEnabled;
    }

    public void setSelectBtnEnabled(boolean selectBtnEnabled) {
        this.selectBtnEnabled = selectBtnEnabled;
    }

    public boolean isDefaultBtnEnabled() {
        return defaultBtnEnabled;
    }

    public void setDefaultBtnEnabled(boolean defaultBtnEnabled) {
        this.defaultBtnEnabled = defaultBtnEnabled;
    }

    public String getDefaultComponentName() {
        return defaultComponentName;
    }

    public void setDefaultComponentName(String defaultComponentName) {
        this.defaultComponentName = defaultComponentName;
    }

    public boolean isEnterKeyAllowed() {
        return enterKeyAllowed;
    }

    public void setEnterKeyAllowed(boolean enterKeyAllowed) {
        this.enterKeyAllowed = enterKeyAllowed;
    }

    public SelectButtonConfig getSelectButtonConfig() {
        return selectButtonConfig;
    }

    public void setSelectButtonConfig(SelectButtonConfig selectButtonConfig) {
        this.selectButtonConfig = selectButtonConfig;
    }

    public DefaultButtonConfig getDefaultButtonConfig() {
        return defaultButtonConfig;
    }

    public void setDefaultButtonConfig(DefaultButtonConfig defaultButtonConfig) {
        this.defaultButtonConfig = defaultButtonConfig;
    }

    @Override
    public String getComponentName() {
        return "editable-table-browser";
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

        EditableTableBrowserConfig that = (EditableTableBrowserConfig) o;

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) :
                that.collectionRefConfig != null) {
            return false;
        }
        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.collectionViewRefConfig) :
                that.collectionViewRefConfig != null) {
            return false;
        }

        if (pageSize != null ? !pageSize.equals(that.pageSize) : that.pageSize != null) {
            return false;
        }

        if (displayChosenValues != null ? !displayChosenValues.equals(that.displayChosenValues) :
                that.displayChosenValues != null) {
            return false;
        }

        if (selectionStyleConfig != null ? !selectionStyleConfig.equals(that.selectionStyleConfig) :
                that.selectionStyleConfig != null) {
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
        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.inputTextFilterConfig) :
                that.inputTextFilterConfig != null) {
            return false;
        }
        if (displayValuesAsLinksConfig != null ? !displayValuesAsLinksConfig.equals(that.displayValuesAsLinksConfig) :
                that.displayValuesAsLinksConfig != null) {
            return false;
        }
        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) :
                that.singleChoice != null) {
            return false;
        }
        if (selectionPatternConfig != null ? !selectionPatternConfig.equals(that.selectionPatternConfig) :
                that.selectionPatternConfig != null) {
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
        if (selectButtonConfig != null ? !selectButtonConfig.equals(that.selectButtonConfig)
                : that.selectButtonConfig != null) {
            return false;
        }
        if (defaultButtonConfig != null ? !defaultButtonConfig.equals(that.defaultButtonConfig)
                : that.defaultButtonConfig != null) {
            return false;
        }
        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig)
                : that.collectionExtraFiltersConfig != null) {
            return false;
        }
        if (resizable != that.resizable) {
            return false;
        }
        if (enterKeyAllowed != that.enterKeyAllowed) {
            return false;
        }
        if (selectBtnEnabled != that.selectBtnEnabled) {
            return false;
        }
        if (defaultBtnEnabled != that.defaultBtnEnabled) {
            return false;
        }
        if (defaultComponentName != null ? !defaultComponentName.equals(that.defaultComponentName) : that.defaultComponentName != null) {
            return false;
        }
        if (dependOnWidgetsIds != null ? !dependOnWidgetsIds.equals(that.dependOnWidgetsIds) : that.dependOnWidgetsIds != null) {
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
        result = 31 * result + (defaultComponentName != null ? defaultComponentName.hashCode() : 0);
        result = 31 * result + (dependOnWidgetsIds != null ? dependOnWidgetsIds.hashCode() : 0);
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (displayChosenValues != null ? displayChosenValues.hashCode() : 0);
        result = 31 * result + (selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0);
        result = 31 * result + (dialogWindowConfig != null ? dialogWindowConfig.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (displayValuesAsLinksConfig != null ? displayValuesAsLinksConfig.hashCode() : 0);
        result = 31 * result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        result = 31 * result + (selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0);
        result = 31 * result + (initialFiltersConfig != null ? initialFiltersConfig.hashCode() : 0);
        result = 31 * result + (selectionFiltersConfig != null ? selectionFiltersConfig.hashCode() : 0);
        result = 31 * result + (selectionSortCriteriaConfig != null ? selectionSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (collectionExtraFiltersConfig != null ? collectionExtraFiltersConfig.hashCode() : 0);
        result = 31 * result + (selectButtonConfig != null ? selectButtonConfig.hashCode() : 0);
        result = 31 * result + (defaultButtonConfig != null ? defaultButtonConfig.hashCode() : 0);
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        result = 31 * result + (resizable ? 1 : 0);
        result = 31 * result + (enterKeyAllowed ? 1 : 0);
        result = 31 * result + (defaultBtnEnabled ? 1 : 0);
        result = 31 * result + (selectBtnEnabled ? 1 : 0);
        return result;
    }
}
