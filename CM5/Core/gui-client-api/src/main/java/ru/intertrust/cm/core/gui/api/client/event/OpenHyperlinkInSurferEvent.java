package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.06.2015
 *         Time: 0:14
 */
public class OpenHyperlinkInSurferEvent extends GwtEvent<OpenHyperlinkInSurferEventHandler> {
    public static Type<OpenHyperlinkInSurferEventHandler> TYPE = new Type<OpenHyperlinkInSurferEventHandler>();
    private Id id;
    private List<LinkedFormConfig> linkedFormMappings;
    private PluginCloseListener pluginCloseListener;
    private boolean editable;

    public OpenHyperlinkInSurferEvent(Id id, List<LinkedFormConfig> linkedFormMappings, PluginCloseListener listener, boolean editable) {
        this.id = id;
        this.linkedFormMappings = linkedFormMappings;
        this.pluginCloseListener = listener;
        this.editable = editable;
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

    public List<LinkedFormConfig> getLinkedFormMappings() {
        return linkedFormMappings;
    }

    public boolean isEditable() {
        return editable;
    }
}

