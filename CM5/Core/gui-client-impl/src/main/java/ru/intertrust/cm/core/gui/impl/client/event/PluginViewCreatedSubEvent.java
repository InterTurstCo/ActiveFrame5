package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class PluginViewCreatedSubEvent extends GwtEvent<PluginViewCreatedSubEventHandler> {
    public static Type<PluginViewCreatedSubEventHandler> TYPE = new Type<PluginViewCreatedSubEventHandler>();
    private float widthRatio;
    private float heightRatio;
    public PluginViewCreatedSubEvent(float widthRatio, float heightRatio){
           this.widthRatio = widthRatio;
           this.heightRatio = heightRatio;
    }
    @Override
    public Type<PluginViewCreatedSubEventHandler> getAssociatedType() {
        return TYPE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void dispatch(PluginViewCreatedSubEventHandler handler) {
       handler.setSizes(widthRatio, heightRatio);
    }
}
