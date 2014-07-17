package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipWidget;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkWidget extends TooltipWidget implements HyperlinkStateChangedEventHandler {

    private String selectionPattern;
    private FormattingConfig formattingConfig;

    @Override
    public Component createNew() {
        return new LinkedDomainObjectHyperlinkWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        initialData = currentState;
        selectionPattern = state.getWidgetConfig().getPatternConfig().getValue();
        formattingConfig = state.getWidgetConfig().getFormattingConfig();
        LinkedHashMap<Id, String> listValues = state.getListValues();
        displayHyperlinks(listValues);
        if (shouldDrawTooltipButton()) {
            ((HyperlinkNoneEditablePanel) (impl)).addShowTooltipLabel(new ShowTooltipHandler());
        }
    }

    @Override
    public WidgetState createNewState() {
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        return state;
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        LinkedDomainObjectHyperlinkState linkedDomainObjectHyperlinkState = (LinkedDomainObjectHyperlinkState) state;
        SelectionStyleConfig selectionStyleConfig = linkedDomainObjectHyperlinkState.getWidgetConfig().getSelectionStyleConfig();
        return new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        PopupPanel popupPanel = event.getPopupPanel();

        if (popupPanel != null) {
            popupPanel.hide();
            fetchWidgetItems();
            return;
        }
        Id id = event.getId();

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
                LinkedHashMap<Id, String> listValues = getUpdatedHyperlinks(id, representation);
                displayHyperlinks(listValues);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });
    }

    private void displayHyperlinks(LinkedHashMap<Id, String> listValues) {
        if (listValues == null) {
            return;
        }
        HyperlinkNoneEditablePanel panel = (HyperlinkNoneEditablePanel) impl;
        panel.cleanPanel();
        panel.displayHyperlinks(listValues);

    }

    private LinkedHashMap<Id, String> getUpdatedHyperlinks(Id id, String representation) {
        LinkedDomainObjectHyperlinkState state = getInitialData();
        LinkedHashMap<Id, String> listValues = state.getListValues();
        listValues.put(id, representation);
        return listValues;
    }

    @Override
    protected String getTooltipHandlerName() {
        return "linked-domain-object-hyperlink";
    }
}

