package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.09.14
 *         Time: 15:58
 */
public class ExpandHierarchicalCollectionData implements Dto {

    private List<ChildCollectionViewerConfig> childCollectionViewerConfigs;
    private Id selectedParentId;
    private String currentCollectionName;

    public ExpandHierarchicalCollectionData() {
    }

    public ExpandHierarchicalCollectionData(List<ChildCollectionViewerConfig> childCollectionViewerConfigs,
                                            Id selectedParentId, String currentCollectionName) {

        this.childCollectionViewerConfigs = childCollectionViewerConfigs;
        this.selectedParentId = selectedParentId;
        this.currentCollectionName = currentCollectionName;
    }

    public List<ChildCollectionViewerConfig> getChildCollectionViewerConfigs() {
        return childCollectionViewerConfigs;
    }

    public Id getSelectedParentId() {
        return selectedParentId;
    }

    public String getCurrentCollectionName() {
        return currentCollectionName;
    }
}
