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
    private Boolean displayOnlySelectedIds;
    private Boolean displayCheckBoxes;
    private Boolean tooltipLimitation;

    public OpenCollectionRequestEvent() {

    }

    public OpenCollectionRequestEvent(PluginPanel pluginPanel, Boolean displayOnlySelectedIds, Boolean displayCheckBoxes, Boolean tooltipLimitation) {
        this.pluginPanel = pluginPanel;
        this.displayOnlySelectedIds = displayOnlySelectedIds;
        this.displayCheckBoxes = displayCheckBoxes;
        this.tooltipLimitation = tooltipLimitation;
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

    public Boolean isDisplayOnlySelectedIds() {
        return displayOnlySelectedIds;
    }

    public Boolean isDisplayCheckBoxes() {
        return displayCheckBoxes;
    }

    public Boolean isTooltipLimitation() {
        return tooltipLimitation;
    }
}
