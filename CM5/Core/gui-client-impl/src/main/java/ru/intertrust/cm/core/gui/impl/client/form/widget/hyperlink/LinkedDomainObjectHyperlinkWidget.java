package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipWidget;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
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

    private LinkedDomainObjectHyperlinkConfig config;

    public LinkedDomainObjectHyperlinkWidget() {
    }

    @Override
    public Component createNew() {
        return new LinkedDomainObjectHyperlinkWidget();
    }

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    public void setCurrentState(WidgetState currentState) {
        LinkedDomainObjectHyperlinkState state = (LinkedDomainObjectHyperlinkState) currentState;
        config = ((LinkedDomainObjectHyperlinkState) currentState).getWidgetConfig();
        initialData = currentState;

        LinkedHashMap<Id, String> listValues = state.getListValues();
        HyperlinkNoneEditablePanel panel = (HyperlinkNoneEditablePanel) impl;
        panel.displayHyperlinks(listValues, WidgetUtil.shouldDrawTooltipButton(state));

    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public WidgetState createNewState() {
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        LinkedDomainObjectHyperlinkState currentState = getInitialData();
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        state.setWidgetConfig(config);
        state.setConfig(currentState.getConfig());
        state.setSelectedIds(currentState.getIds());
        return state;
    }

    @Override
    protected boolean isChanged() {
        return false;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        localEventBus.addHandler(ShowTooltipEvent.TYPE, this);
        LinkedDomainObjectHyperlinkState linkedDomainObjectHyperlinkState = (LinkedDomainObjectHyperlinkState) state;
        SelectionStyleConfig selectionStyleConfig = linkedDomainObjectHyperlinkState.getWidgetConfig().getSelectionStyleConfig();
        boolean hyperlinkInModalWindow = linkedDomainObjectHyperlinkState.getWidgetConfig().isModalWindow();
        return new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus, false,
                linkedDomainObjectHyperlinkState.getTypeTitleMap(), this).withHyperlinkModalWindow(hyperlinkInModalWindow);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return asEditableWidget(state);
    }

    //TODO common method
    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        final HyperlinkDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
        Id id = event.getId();
        List<Id> ids = new ArrayList<Id>();
        ids.add(id);
        String collectionName = config.getCollectionRefConfig() == null ? null : config.getCollectionRefConfig().getName();
        RepresentationRequest request = new RepresentationRequest(ids, config.getSelectionPatternConfig().getValue(),
                collectionName, config.getFormattingConfig());
        Command command = new Command("getRepresentationForOneItem", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                String representation = response.getRepresentation();
                Id id = response.getId();
                LinkedHashMap<Id, String> listValues = getUpdatedHyperlinks(id, representation);
                LinkedDomainObjectHyperlinkState state = getInitialData();
                hyperlinkDisplay.displayHyperlinks(listValues, WidgetUtil.shouldDrawTooltipButton(state));

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
        return getName();
    }

    @Override
    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return config.getLinkedFormMappingConfig();
    }

    @Override
    public LinkedFormConfig getLinkedFormConfig() {
        return config.getLinkedFormConfig();
    }

}

