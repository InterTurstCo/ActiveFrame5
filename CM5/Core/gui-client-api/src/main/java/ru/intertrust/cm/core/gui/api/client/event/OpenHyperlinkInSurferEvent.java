package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.06.2015
 *         Time: 0:14
 */
public class OpenHyperlinkInSurferEvent extends GwtEvent<OpenHyperlinkInSurferEventHandler> {


    public static Type<OpenHyperlinkInSurferEventHandler> TYPE = new Type<OpenHyperlinkInSurferEventHandler>();
    private Id id;
    private LinkedFormMappingConfig linkedFormMappingConfig;
    private PluginCloseListener pluginCloseListener;

    public OpenHyperlinkInSurferEvent(Id id, LinkedFormMappingConfig linkedFormMappingConfig, PluginCloseListener listener) {
        this.id = id;
        this.linkedFormMappingConfig = linkedFormMappingConfig;
        this.pluginCloseListener = listener;
    }

    @Override
    public Type<OpenHyperlinkInSurferEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(OpenHyperlinkInSurferEventHandler handler) {
        handler.onOpenHyperlinkInSurfer(this);
    }

    public Id getId() {
        return id;
    }

    public PluginCloseListener getPluginCloseListener() {
        return pluginCloseListener;
    }

    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedFormMappingConfig;
    }
}

