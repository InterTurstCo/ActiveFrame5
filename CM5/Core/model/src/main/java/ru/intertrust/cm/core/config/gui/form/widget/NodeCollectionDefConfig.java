package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.hierarchybrowser.CreateNewButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 11:40
 */
@Root(name = "node-collection-def")
public class NodeCollectionDefConfig implements Dto {
    @Attribute(name = "collection")
    private String collection;

    @Attribute(name = "parent-filter", required = false)
    private String parentFilter;
    @Deprecated
    @Attribute(name = "title", required = false)
    private String title;

    @Attribute(name = "selective", required = false)
    private boolean selective = true;

    @Attribute(name = "display-create-button", required = false)
    private boolean displayCreateButton = true;

    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    @Element(name = "fill-parent-on-add", required = false)
    private FillParentOnAddConfig fillParentOnAddConfig;

    @Element(name = "root-node-link", required = false)
    private RootNodeLinkConfig rootNodeLinkConfig;

    @Element(name = "selection-filters", required = false)
    private SelectionFiltersConfig selectionFiltersConfig;

    @ElementList(inline = true, name ="node-collection-def", required = false)
    private List<NodeCollectionDefConfig> nodeCollectionDefConfigs = new ArrayList<NodeCollectionDefConfig>();

    @Element(name = "selection-sort-criteria",required = false)
    private SelectionSortCriteriaConfig selectionSortCriteriaConfig;

    @Element(name = "create-new-button", required = false)
    private CreateNewButtonConfig createNewButtonConfig;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoiceConfig;

    @Element(name = "display-values-as-links", required = false)
    private DisplayValuesAsLinksConfig displayValuesAsLinksConfig;

    @Element(name = "linked-form-mapping", required = false)
    private LinkedFormMappingConfig linkedFormMappingConfig;

    @Element(name = "created-objects", required = false)
    private CreatedObjectsConfig createdObjectsConfig;

    private int elementsCount;

    private Map<String, PopupTitlesHolder> doTypeTitlesMap;

    public SelectionPatternConfig getSelectionPatternConfig() {
        return selectionPatternConfig;
    }

    public void setSelectionPatternConfig(SelectionPatternConfig selectionPatternConfig) {
        this.selectionPatternConfig = selectionPatternConfig;
    }

    public boolean isSelective() {
        return selective;
    }

    public void setSelective(boolean selective) {
        this.selective = selective;
    }

    public InputTextFilterConfig getInputTextFilterConfig() {
        return inputTextFilterConfig;
    }

    public void setInputTextFilterConfig(InputTextFilterConfig inputTextFilterConfig) {
        this.inputTextFilterConfig = inputTextFilterConfig;
    }

    public List<NodeCollectionDefConfig> getNodeCollectionDefConfigs() {
        return nodeCollectionDefConfigs;
    }

    public void setNodeCollectionDefConfigs(List<NodeCollectionDefConfig> nodeCollectionDefConfigs) {
        this.nodeCollectionDefConfigs = nodeCollectionDefConfigs;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getParentFilter() {
        return parentFilter;
    }

    public void setParentFilter(String parentFilter) {
        this.parentFilter = parentFilter;
    }

    public FillParentOnAddConfig getFillParentOnAddConfig() {
        return fillParentOnAddConfig;
    }

    public void setFillParentOnAddConfig(FillParentOnAddConfig fillParentOnAddConfig) {
        this.fillParentOnAddConfig = fillParentOnAddConfig;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }
    @Deprecated
    public String getTitle() {
        return title;
    }

    @Deprecated
    public void setTitle(String title) {
        this.title = title;
    }

    public RootNodeLinkConfig getRootNodeLinkConfig() {
        return rootNodeLinkConfig;
    }

    public void setRootNodeLinkConfig(RootNodeLinkConfig rootNodeLinkConfig) {
        this.rootNodeLinkConfig = rootNodeLinkConfig;
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

    public boolean isDisplayingCreateButton() {
        return displayCreateButton;
    }

    public void setDisplayCreateButton(boolean displayCreateButton) {
        this.displayCreateButton = displayCreateButton;
    }

    public CreateNewButtonConfig getCreateNewButtonConfig() {
        return createNewButtonConfig;
    }

    public void setCreateNewButtonConfig(CreateNewButtonConfig createNewButtonConfig) {
        this.createNewButtonConfig = createNewButtonConfig;
    }

    public SingleChoiceConfig getSingleChoiceConfig() {
        return singleChoiceConfig;
    }

    public void setSingleChoiceConfig(SingleChoiceConfig singleChoiceConfig) {
        this.singleChoiceConfig = singleChoiceConfig;
    }

    public DisplayValuesAsLinksConfig getDisplayValuesAsLinksConfig() {
        return displayValuesAsLinksConfig;
    }

    public void setDisplayValuesAsLinksConfig(DisplayValuesAsLinksConfig displayValuesAsLinksConfig) {
        this.displayValuesAsLinksConfig = displayValuesAsLinksConfig;
    }

    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedFormMappingConfig;
    }

    public void setLinkedFormMappingConfig(LinkedFormMappingConfig linkedFormMappingConfig) {
        this.linkedFormMappingConfig = linkedFormMappingConfig;
    }

    public int getElementsCount() {
        return elementsCount;
    }

    public void setElementsCount(int elementsCount) {
        this.elementsCount = elementsCount;
    }

    public CreatedObjectsConfig getCreatedObjectsConfig() {
        return createdObjectsConfig;
    }

    public void setCreatedObjectsConfig(CreatedObjectsConfig createdObjectsConfig) {
        this.createdObjectsConfig = createdObjectsConfig;
    }

    public Map<String, PopupTitlesHolder> getDoTypeTitlesMap() {
        return doTypeTitlesMap;
    }

    public void setDoTypeTitlesMap(Map<String, PopupTitlesHolder> doTypeTitlesMap) {
        this.doTypeTitlesMap = doTypeTitlesMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeCollectionDefConfig that = (NodeCollectionDefConfig) o;

        if (nodeCollectionDefConfigs != null ? !nodeCollectionDefConfigs.
                equals(that.nodeCollectionDefConfigs) : that.nodeCollectionDefConfigs != null) {
            return false;
        }

        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.
                inputTextFilterConfig) : that.inputTextFilterConfig != null) {
            return false;
        }
        if (selectionPatternConfig != null ? !selectionPatternConfig.equals(that.
                selectionPatternConfig) : that.selectionPatternConfig != null) {
            return false;
        }
        if (parentFilter != null ? !parentFilter.equals(that.parentFilter) : that.parentFilter != null) {
            return false;
        }
        if (fillParentOnAddConfig != null ? !fillParentOnAddConfig.equals(that.fillParentOnAddConfig) : that.
                fillParentOnAddConfig != null) {
            return false;
        }

        if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
            return false;
        }

        if (selective != that.selective) {
            return false;
        }

        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) :
                that.defaultSortCriteriaConfig != null) {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        if (rootNodeLinkConfig != null ? !rootNodeLinkConfig.equals(that.rootNodeLinkConfig) :
                that.rootNodeLinkConfig != null) {
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
        if (displayCreateButton != that.displayCreateButton) {
            return false;
        }
        if (createNewButtonConfig != null ? !createNewButtonConfig.equals(that.createNewButtonConfig) :
                that.createNewButtonConfig != null) {
            return false;
        }
        if (singleChoiceConfig != null ? !singleChoiceConfig.equals(that.singleChoiceConfig) :
                that.singleChoiceConfig != null) {
            return false;
        }
        if (displayValuesAsLinksConfig != null ? !displayValuesAsLinksConfig.equals(that.displayValuesAsLinksConfig) :
                that.displayValuesAsLinksConfig != null) {
            return false;
        }
        if (linkedFormMappingConfig != null ? !linkedFormMappingConfig.equals(that.linkedFormMappingConfig)
                : that.linkedFormMappingConfig != null) {
            return false;
        }
        if (createdObjectsConfig != null ? !createdObjectsConfig.equals(that.createdObjectsConfig) :
                that.createdObjectsConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0;
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (nodeCollectionDefConfigs != null ? nodeCollectionDefConfigs.hashCode() : 0);
        result = 31 * result + (fillParentOnAddConfig != null ? fillParentOnAddConfig.hashCode() : 0);
        result = 31 * result + (parentFilter != null ? parentFilter.hashCode() : 0);
        result = 31 * result + (collection != null ? collection.hashCode() : 0);
        result = 31 * result + (selective ? 1 : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (rootNodeLinkConfig != null ? rootNodeLinkConfig.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (selectionFiltersConfig != null ? selectionFiltersConfig.hashCode() : 0);
        result = 31 * result + (selectionSortCriteriaConfig != null ? selectionSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (createNewButtonConfig != null ? createNewButtonConfig.hashCode() : 0);
        result = 31 * result + (singleChoiceConfig != null ? singleChoiceConfig.hashCode() : 0);
        result = 31 * result + (displayValuesAsLinksConfig != null ? displayValuesAsLinksConfig.hashCode() : 0);
        result = 31 * result + (linkedFormMappingConfig != null ? linkedFormMappingConfig.hashCode() : 0);
        result = 31 * result + (createdObjectsConfig != null ? createdObjectsConfig.hashCode() : 0);
        result = 31 * result + (displayCreateButton ? 1 : 0);
        return result;
    }
}
