package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchDomainObjectSurfacePluginData;

/**
 * User: IPetrov
 * Date: 21.01.14
 * Time: 17:34
 * Событие завершения расширенного поиска
 */
public class ExtendedSearchCompleteEvent extends GwtEvent<ExtendedSearchCompleteEventHandler> {

    public static GwtEvent.Type<ExtendedSearchCompleteEventHandler> TYPE = new GwtEvent.Type<ExtendedSearchCompleteEventHandler>();
    // данные плагина
    private ExtendedSearchDomainObjectSurfacePluginData domainObjectSurferPluginData;

    public ExtendedSearchCompleteEvent(ExtendedSearchDomainObjectSurfacePluginData domainObjectSurferPluginData) {
        this.domainObjectSurferPluginData = domainObjectSurferPluginData;
    }

    @Override
    public GwtEvent.Type<ExtendedSearchCompleteEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(ExtendedSearchCompleteEventHandler handler) {
        handler.onExtendedSearchComplete(this);
    }

    public ExtendedSearchDomainObjectSurfacePluginData getDomainObjectSurferPluginData() {
        return domainObjectSurferPluginData;
    }
}
