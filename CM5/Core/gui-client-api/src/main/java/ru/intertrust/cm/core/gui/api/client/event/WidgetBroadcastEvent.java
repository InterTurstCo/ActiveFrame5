package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;

import java.util.HashSet;
import java.util.Set;

public class WidgetBroadcastEvent extends GwtEvent<WidgetBroadcastEventHandler> {
  private String widgetId;
  private String value;
  private Boolean cascade = false;
  private Boolean isBroadcast;
  private Set<String> publicatorsChain;
  private int initiatorHashCode;
  private int initiatorToolBarHashCode;
  private Object eventPayload;

  public static final Type<WidgetBroadcastEventHandler> TYPE = new Type<>();

  public WidgetBroadcastEvent(Object payload,String aWidgetId, int anInitiatorHashCode,int toolBarHashCode) {
    widgetId = aWidgetId;
    initiatorHashCode = anInitiatorHashCode;
    initiatorToolBarHashCode = toolBarHashCode;
    publicatorsChain = new HashSet<>();
    eventPayload = payload;
  }

  public WidgetBroadcastEvent(Object payload,Boolean isBroadcast){
    this.isBroadcast = true;
    eventPayload = payload;
  }

  public WidgetBroadcastEvent(Object payload,String aWidgetId,int anInitiatorHashCode,int toolBarHashCode, Boolean isCascade) {
    this(payload,aWidgetId, anInitiatorHashCode,toolBarHashCode);
    cascade = isCascade;
    if(cascade){
      publicatorsChain.add(widgetId);
    }
  }

  public WidgetBroadcastEvent(Object payload,String aWidgetId, int anInitiatorHashCode,int toolBarHashCode, String aValue) {
    this(payload,aWidgetId,anInitiatorHashCode,toolBarHashCode, false);
    value = aValue;
  }

  public WidgetBroadcastEvent(Object payload,String aWidgetId,int anInitiatorHashCode,int toolBarHashCode, String aValue, Boolean isCascade) {
    this(payload,aWidgetId,anInitiatorHashCode,toolBarHashCode, isCascade);
    value = aValue;
  }


  @Override
  public Type<WidgetBroadcastEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(WidgetBroadcastEventHandler widgetBroadcastEventHandler) {
    widgetBroadcastEventHandler.onEventReceived(this);
  }

  public Boolean getBroadcast() {
    return isBroadcast;
  }

  public void setBroadcast(Boolean broadcast) {
    isBroadcast = broadcast;
  }

  public String getWidgetId() {
    return widgetId;
  }

  public String getValue() {
    return value;
  }

  public Boolean getCascade() {
    return cascade;
  }

  public Set<String> getPublicatorsChain() {
    return publicatorsChain;
  }

  public int getInitiatorHashCode() {
    return initiatorHashCode;
  }

  public int getInitiatorToolBarHashCode() {
    return initiatorToolBarHashCode;
  }

  public Object getEventPayload() {
    return eventPayload;
  }

  public void setEventPayload(Object eventPayload) {
    this.eventPayload = eventPayload;
  }
}
