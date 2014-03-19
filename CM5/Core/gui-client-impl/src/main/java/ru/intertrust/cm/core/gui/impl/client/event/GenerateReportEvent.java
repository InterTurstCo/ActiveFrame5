package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Lesia Puhova
 *         Date: 19.03.14
 *         Time: 12:10
 */
public class GenerateReportEvent extends GwtEvent<GenerateReportEventHandler> {

    public static final GwtEvent.Type<GenerateReportEventHandler> TYPE = new GwtEvent.Type<GenerateReportEventHandler>();

    @Override
    public Type<GenerateReportEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(GenerateReportEventHandler handler) {
        handler.generateReport();
    }
}
