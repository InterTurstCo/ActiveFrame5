package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 12.09.14
 *         Time: 18:47
 */
public class HierarchicalCollectionEvent extends GwtEvent<HierarchicalCollectionEventHandler> {

    public static final Type<HierarchicalCollectionEventHandler> TYPE = new Type<HierarchicalCollectionEventHandler>();

    private Id selectedId;
    private List<ChildCollectionViewerConfig> childCollectionViewerConfigs;

    public HierarchicalCollectionEvent(Id selectedId, List<ChildCollectionViewerConfig> childCollectionViewerConfigs) {
        this.selectedId = selectedId;
        this.childCollectionViewerConfigs = childCollectionViewerConfigs;
    }

    public Id getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Id selectedId) {
        this.selectedId = selectedId;
    }

    public List<ChildCollectionViewerConfig> getChildCollectionViewerConfigs() {
        return childCollectionViewerConfigs;
    }

    public void setChildCollectionViewerConfigs(List<ChildCollectionViewerConfig> childCollectionViewerConfigs) {
        this.childCollectionViewerConfigs = childCollectionViewerConfigs;
    }

    @Override
    public Type<HierarchicalCollectionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchicalCollectionEventHandler handler) {
        handler.onExpandHierarchyEvent(this);
    }
}
