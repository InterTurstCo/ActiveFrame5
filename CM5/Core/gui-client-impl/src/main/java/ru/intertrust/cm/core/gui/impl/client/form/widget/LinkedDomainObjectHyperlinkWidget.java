package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkWidget extends BaseWidget implements HyperlinkStateChangedEventHandler {
    private EventBus localEventBus = new SimpleEventBus();
    private String selectionPattern;
    private FormattingConfig formattingConfig;
    @Override
    public Component createNew() {
        return new LinkedDomainObjectHyperlinkWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        final LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        selectionPattern = state.getSelectionPattern();
        formattingConfig = state.getFormattingConfig();
        List<HyperlinkItem> hyperlinkItems = state.getHyperlinkItems();
        displayHyperlinks(hyperlinkItems);
    }

    @Override
    public WidgetState createNewState() {
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        LinkedDomainObjectHyperlinkState linkedDomainObjectHyperlinkState = (LinkedDomainObjectHyperlinkState) state;
        SelectionStyleConfig selectionStyleConfig = linkedDomainObjectHyperlinkState.getSelectionStyleConfig();
        return new SimpleNoneEditablePanelWithHyperlinks(selectionStyleConfig, localEventBus);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
      return  asEditableWidget(state);
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        Id id = event.getId();
        updateHyperlink(id);
    }

    private void displayHyperlinks(List<HyperlinkItem> hyperlinkItems) {
        if (hyperlinkItems == null) {
            return;
        }
        SimpleNoneEditablePanelWithHyperlinks panel = (SimpleNoneEditablePanelWithHyperlinks) impl;
        panel.cleanPanel();
        for (HyperlinkItem hyperlinkItem : hyperlinkItems) {
            Id id = hyperlinkItem.getId();
            String representation = hyperlinkItem.getRepresentation();
            panel.displayHyperlink(id, representation);
        }
    }
    private List<HyperlinkItem> getUpdatedHyperlinks(HyperlinkItem updatedItem) {
        LinkedDomainObjectHyperlinkState state = getInitialData();
        Id idToFind = updatedItem.getId();
        List<HyperlinkItem> items = state.getHyperlinkItems();
        for (HyperlinkItem item : items) {
            if (idToFind.equals(item.getId())) {
                int index = items.indexOf(item);
                items.set(index, updatedItem);
            }
        }
        return items;
    }

    private void updateHyperlink(Id id) {
        List<Id> ids = new ArrayList<Id>();
        ids.add(id);
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, formattingConfig);
        Command command = new Command("getRepresentationForOneItem", "representation-updater", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                String representation = response.getRepresentation();
                Id id = response.getId();
                HyperlinkItem updatedItem = new HyperlinkItem(id, representation);
                List<HyperlinkItem> updatedHyperlinks = getUpdatedHyperlinks(updatedItem);
                displayHyperlinks(updatedHyperlinks);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }
}

