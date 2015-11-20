package ru.intertrust.performance.gwtrpcproxy;

public interface ProxyContextListener {
    void onAddGroup(GwtInteractionGroup group);
    void onAddGwtInteraction(GwtInteraction requestResponse);
}
