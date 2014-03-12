package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.LinkedDomainObjectHyperlinkItem;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkUpdateRequest;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkUpdateResponse;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkWidget extends BaseWidget implements HyperlinkStateChangedEventHandler {
    private EventBus localEventBus = new SimpleEventBus();
    private String selectionPattern;

    @Override
    public Component createNew() {
        return new LinkedDomainObjectHyperlinkWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        final LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        selectionPattern = state.getSelectionPattern();
        String title = state.getDomainObjectType();
        Id id = state.getId();

            LinkedDomainObjectHyperlinkItem hyperlinkItem = (LinkedDomainObjectHyperlinkItem) impl;
            hyperlinkItem.setText(state.getStringRepresentation());
            hyperlinkItem.addItemClickHandler(new HyperlinkClickHandler(title, id, localEventBus));

    }

    @Override
    public WidgetState createNewState() {
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        return new LinkedDomainObjectHyperlinkItem(true);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        return new LinkedDomainObjectHyperlinkItem(false);
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        Id id = event.getId();
        updateHyperlink(id);
    }

    private void updateHyperlink(Id id) {
        HyperlinkUpdateRequest request = new HyperlinkUpdateRequest(id, selectionPattern);
        Command command = new Command("updateHyperlink", "linked-domain-object-hyperlink", request);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HyperlinkUpdateResponse response = (HyperlinkUpdateResponse) result;
                String representation = response.getRepresentation();

                    LinkedDomainObjectHyperlinkItem item = (LinkedDomainObjectHyperlinkItem) impl;
                    item.setText(representation);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }
}

