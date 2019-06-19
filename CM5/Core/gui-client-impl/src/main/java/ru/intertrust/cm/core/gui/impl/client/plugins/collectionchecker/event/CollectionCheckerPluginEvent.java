package ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker.event;

import com.google.gwt.event.shared.GwtEvent;

public class CollectionCheckerPluginEvent extends GwtEvent<CollectionCheckerPluginEventHandler> {

  private PluginActions action;

  public static Type<CollectionCheckerPluginEventHandler> TYPE = new Type<>();

  public CollectionCheckerPluginEvent(PluginActions anAction){
    action = anAction;
  }

  @Override
  public Type<CollectionCheckerPluginEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CollectionCheckerPluginEventHandler handler) {
    handler.dispatch(this);
  }

  public PluginActions getAction() {
    return action;
  }
}
