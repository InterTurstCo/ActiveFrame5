package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.InputTextFilterConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 20/9/13
 *         Time: 12:05 PM
 */
@Root(name = "collection-viewer")
public class CollectionViewerConfig extends PluginConfig{
    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @ElementList(inline = true)
    private List<SortCriterionConfig> sortCriterionConfigList = new ArrayList<SortCriterionConfig>();

    private List<Id> excludedIds = new ArrayList<Id>();

    private boolean displayChosenValues;

    private boolean singleChoice = true;

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public List<SortCriterionConfig> getSortCriterionConfigList() {
        return sortCriterionConfigList;
    }

    public void setSortCriterionConfigList(List<SortCriterionConfig> sortCriterionConfigList) {
        this.sortCriterionConfigList = sortCriterionConfigList;
    }

    public CollectionViewRefConfig getCollectionViewRefConfig() {
        return collectionViewRefConfig;
    }

    public void setCollectionViewRefConfig(CollectionViewRefConfig collectionViewRefConfig) {
        this.collectionViewRefConfig = collectionViewRefConfig;
    }

    public InputTextFilterConfig getInputTextFilterConfig() {
        return inputTextFilterConfig;
    }

    public void setInputTextFilterConfig(InputTextFilterConfig inputTextFilterConfig) {
        this.inputTextFilterConfig = inputTextFilterConfig;
    }

    public List<Id> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<Id> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public void setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
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

        CollectionViewerConfig that = (CollectionViewerConfig) o;

        if (sortCriterionConfigList != null ? !sortCriterionConfigList.equals(that.getSortCriterionConfigList()) : that.
                getSortCriterionConfigList() != null) {
            return false;
        }

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.getCollectionRefConfig()) : that.
                getCollectionRefConfig() != null) {
            return false;
        }

        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.getCollectionViewRefConfig()) : that.
                getCollectionViewRefConfig() != null) {
            return false;
        }
        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.inputTextFilterConfig) : that.
                inputTextFilterConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionRefConfig != null ? collectionRefConfig.hashCode() : 0;
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (sortCriterionConfigList != null ? sortCriterionConfigList.hashCode() : 0);
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "collection.plugin";
    }
}

