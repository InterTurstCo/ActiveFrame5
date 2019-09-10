package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class LeftPanelAttachedEvent extends GwtEvent<LeftPanelAttachedEventHandler> {

  private Boolean isAttached = false;

  public static Type<LeftPanelAttachedEventHandler> TYPE = new Type<LeftPanelAttachedEventHandler>();

  @Override
  public Type<LeftPanelAttachedEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(LeftPanelAttachedEventHandler handler) {
    handler.onLeftPanelAttachedEvent(this);
  }

  public LeftPanelAttachedEvent(Boolean attached){
    this.isAttached = attached;
  }

  public Boolean getAttached() {
    return isAttached;
  }
}
