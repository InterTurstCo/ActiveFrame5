package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserSearchClickEvent extends GwtEvent<HierarchyBrowserSearchClickEventHandler> {

    public static Type<HierarchyBrowserSearchClickEventHandler> TYPE = new Type<HierarchyBrowserSearchClickEventHandler>();
    private NodeMetadata nodeMetadata;
    private String inputText;
    public HierarchyBrowserSearchClickEvent(NodeMetadata nodeMetadata, String inputText) {
        this.nodeMetadata = nodeMetadata;
        this.inputText = inputText;
    }

    @Override
    public Type<HierarchyBrowserSearchClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserSearchClickEventHandler handler) {
        handler.onHierarchyBrowserSearchClick(this);
    }

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }

    public String getInputText() {
        return inputText;
    }
}