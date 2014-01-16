package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserScrollEvent extends GwtEvent<HierarchyBrowserScrollEventHandler> {

    public static Type<HierarchyBrowserScrollEventHandler> TYPE = new Type<HierarchyBrowserScrollEventHandler>();
   private NodeMetadata nodeMetadata;
    private String inputText;
    private int factor;
    public HierarchyBrowserScrollEvent(NodeMetadata nodeMetadata, int factor, String inputText){
        this.nodeMetadata = nodeMetadata;
        this.factor = factor;
        this.inputText = inputText;
    }

    @Override
    public Type<HierarchyBrowserScrollEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserScrollEventHandler handler) {
        handler.onHierarchyBrowserScroll(this);
    }

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }

    public int getFactor() {
        return factor;
    }

    public String getInputText() {
        return inputText;
    }
}