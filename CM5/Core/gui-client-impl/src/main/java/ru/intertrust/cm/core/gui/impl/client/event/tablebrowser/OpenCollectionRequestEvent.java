package ru.intertrust.cm.core.gui.impl.client.event.tablebrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.11.2014
 *         Time: 9:19
 */
public class OpenCollectionRequestEvent extends GwtEvent<OpenCollectionRequestEventHandler> {
    public static final Type<OpenCollectionRequestEventHandler> TYPE = new Type<OpenCollectionRequestEventHandler>();
    private PluginPanel pluginPanel;
    private Boolean displayOnlyChosenIds;
    private Boolean displayCheckBoxes;

    public OpenCollectionRequestEvent() {

    }

    public OpenCollectionRequestEvent(PluginPanel pluginPanel, Boolean displayOnlyChosenIds, Boolean displayCheckBoxes) {
        this.pluginPanel = pluginPanel;
        this.displayOnlyChosenIds = displayOnlyChosenIds;
        this.displayCheckBoxes = displayCheckBoxes;

    }

    @Override
    public Type<OpenCollectionRequestEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(OpenCollectionRequestEventHandler handler) {
        handler.openCollectionView(this);

    }

    public PluginPanel getPluginPanel() {
        return pluginPanel;
    }

    public Boolean isDisplayOnlyChosenIds() {
        return displayOnlyChosenIds;
    }

    public Boolean isDisplayCheckBoxes() {
        return displayCheckBoxes;
    }

}
