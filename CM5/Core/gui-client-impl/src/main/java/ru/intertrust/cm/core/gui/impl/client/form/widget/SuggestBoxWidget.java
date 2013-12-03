package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.MultiWordIdentifiableSuggestion;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends BaseWidget {

    SuggestBoxState currentState;
    HashMap<Id, String> allSuggestions = new HashMap<Id, String>();
    HashMap<Id, String> selectedSuggestions = new HashMap<Id, String>();
    private HorizontalPanel selectedRecords;

    @Override
    public void setCurrentState(WidgetState currentState) {
        selectedRecords.clear();
        SuggestBoxState suggestBoxState = (SuggestBoxState) currentState;
        this.currentState = suggestBoxState;
        LinkedHashMap<Id, String> listValues = suggestBoxState.getObjects();
        for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
            final HorizontalPanel recordContainer = new HorizontalPanel();
            recordContainer.add(new InlineLabel(listEntry.getValue()));
            final HTML closeButton = new HTML();
            closeButton.addStyleName("icon remove ");
            closeButton.getElement().setId(listEntry.getKey().toStringRepresentation());
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    recordContainer.removeFromParent();
                    selectedSuggestions.remove(listEntry.getKey());
                }
            });
            recordContainer.add(closeButton);
            selectedRecords.add(recordContainer);
            selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
        }
    }

    @Override
    public SuggestBoxState getCurrentState() {
        SuggestBoxState state = new SuggestBoxState();
        state.setSelectedIds(new ArrayList<Id>(selectedSuggestions.keySet()));
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        MultiWordSuggestOracle oracle = buildDynamicMultiWordOracle();
        final SuggestBox suggestBox = new SuggestBox(oracle);

        suggestBox.getElement().removeClassName("gwt-SuggestBox");

        selectedRecords = new HorizontalPanel();

        FlowPanel container = new FlowPanel();
        HorizontalPanel hp = new HorizontalPanel();
        hp.setWidth("400px");

        hp.getElement().getStyle().setBorderWidth(2, Style.Unit.PX);
        container.add(hp);
        hp.add(selectedRecords);
        hp.add(suggestBox);




        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                final MultiWordIdentifiableSuggestion selectedItem = (MultiWordIdentifiableSuggestion) event.getSelectedItem();
                final String replacementString = selectedItem.getReplacementString();
                selectedSuggestions.put(selectedItem.getId(), selectedItem.getReplacementString());
                final HorizontalPanel record = new HorizontalPanel();
                record.add(new InlineLabel(replacementString));
                final HTML closeButton = new HTML();
                closeButton.addStyleName("icon remove ");
                closeButton.getElement().setId(selectedItem.getId().toStringRepresentation());
                closeButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        record.removeFromParent();
                        selectedSuggestions.remove(selectedItem.getId());
                    }
                });
                record.add(closeButton);
                selectedRecords.add(record);
                SuggestBox sourceObject = (SuggestBox) event.getSource();

                //clear suggest input filling
                sourceObject.setText("");
            }
        });
        return container;
    }

    private MultiWordSuggestOracle buildDynamicMultiWordOracle() {
        return new MultiWordSuggestOracle() {
            @Override
            public void requestSuggestions(final Request request, final Callback callback) {
                SuggestionRequest suggestionRequest = new SuggestionRequest();

                SuggestBoxConfig suggestBoxConfig = currentState.getSuggestBoxConfig();
                String name = suggestBoxConfig.getCollectionRefConfig().getName();
                suggestionRequest.setCollectionName(name);
                String dropDownPatternConfig = suggestBoxConfig.getDropdownPatternConfig().getValue();

                suggestionRequest.setDropdownPattern(dropDownPatternConfig);
                suggestionRequest.setSelectionPattern(suggestBoxConfig.getSelectionPatternConfig().getValue());
                suggestionRequest.setText(request.getQuery());
                suggestionRequest.setExcludeIds(new LinkedHashSet<Id>(selectedSuggestions.keySet()));
                suggestionRequest.setInputTextFilterName(suggestBoxConfig.getInputTextFilterConfig().getName());
                suggestionRequest.setIdsExclusionFilterName(suggestBoxConfig.getSelectionExcludeFilterConfig().getName());

                Command command = new Command("obtainSuggestions", SuggestBoxWidget.this.getName(), suggestionRequest);
                BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onSuccess(Dto result) {
                        SuggestionList list = (SuggestionList) result;
                        ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
                        allSuggestions.clear();
                        for (SuggestionItem suggestionItem : list.getSuggestions()) {
                            suggestions.add(new MultiWordIdentifiableSuggestion(suggestionItem.getId(), suggestionItem.getReplacementText(), suggestionItem.getDisplayText()));
                            allSuggestions.put(suggestionItem.getId(), suggestionItem.getDisplayText());
                        }
                        Response response = new Response();
                        response.setSuggestions(suggestions);
                        callback.onSuggestionsReady(request, response);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something was going wrong while obtaining suggestions for '" + request.getQuery() + "'");
                    }
                });
                GWT.log("suggestion requested " + request.getQuery());
            }
        };
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

    @Override
    public Component createNew() {
        return new SuggestBoxWidget();
    }
}
