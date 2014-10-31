package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.10.2014
 *         Time: 7:57
 */
public class OpenDomainObjectFormEvent extends GwtEvent<OpenDomainObjectFormEventHandler> {

    public static Type<OpenDomainObjectFormEventHandler> TYPE = new Type<OpenDomainObjectFormEventHandler>();
    private Id id;

    public OpenDomainObjectFormEvent(Id id) {
        this.id = id;

    }

    @Override
    public Type<OpenDomainObjectFormEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(OpenDomainObjectFormEventHandler handler) {
        handler.onOpenDomainObjectFormEvent(this);
    }

    public Id getId() {
        return id;
    }

}
