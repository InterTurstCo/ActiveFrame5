package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
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
public class LinkedDomainObjectHyperlinkWidget extends TooltipWidget implements HyperlinkStateChangedEventHandler, HasLinkedFormMappings {

    private String selectionPattern;
    private FormattingConfig formattingConfig;
    private LinkedDomainObjectHyperlinkConfig linkedDomainObjectHyperlinkConfig;

    public LinkedDomainObjectHyperlinkWidget() {
    }

    @Override
    public Component createNew() {
        return new LinkedDomainObjectHyperlinkWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        linkedDomainObjectHyperlinkConfig = ((LinkedDomainObjectHyperlinkState) currentState).getWidgetConfig();
        initialData = currentState;
        selectionPattern = state.getWidgetConfig().getPatternConfig().getValue();
        formattingConfig = state.getWidgetConfig().getFormattingConfig();
        LinkedHashMap<Id, String> listValues = state.getListValues();
        HyperlinkNoneEditablePanel panel = (HyperlinkNoneEditablePanel) impl;
        panel.displayHyperlinks(listValues, shouldDrawTooltipButton());

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
        return new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus, false,
                linkedDomainObjectHyperlinkState.getTypeTitleMap(), this);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        final HyperlinkDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
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
                hyperlinkDisplay.displayHyperlinks(listValues, shouldDrawTooltipButton());

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });
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

    @Override
    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedDomainObjectHyperlinkConfig.getLinkedFormMappingConfig();
    }
}

