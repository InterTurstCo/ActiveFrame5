package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends BaseWidget {

    SuggestBoxState currentState;
    HashMap<String, Long> allSuggestions = new HashMap<String, Long>();
    HashMap<String, Long> selectedSuggestions = new HashMap<String, Long>();

    @Override
    public void setCurrentState(WidgetState currentState) {
        this.currentState = (SuggestBoxState) currentState;
    }

    @Override
    public WidgetState getCurrentState() {
        return currentState;
    }

    @Override
    protected Widget asEditableWidget() {
        VerticalPanel container = new VerticalPanel();
        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle() {
            @Override
            public void requestSuggestions(final Request request, final Callback callback) {
                SuggestionRequest suggestionRequest = new SuggestionRequest();

                SuggestBoxConfig suggestBoxConfig = currentState.getSuggestBoxConfig();
                String name = suggestBoxConfig.getCollectionRefConfig().getName();
                suggestionRequest.setCollectionName(name);
                String value = suggestBoxConfig.getPatternConfig().getValue();

                suggestionRequest.setPattern(value);
                suggestionRequest.setText(request.getQuery());
                suggestionRequest.setExcludeIds(new ArrayList<Long>(selectedSuggestions.values()));

                Command command = new Command("obtainSuggestions", SuggestBoxWidget.this.getName(), suggestionRequest);
                BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onSuccess(Dto result) {
                        SuggestionList list = (SuggestionList) result;
                        ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
                        allSuggestions.clear();
                        for (SuggestionItem suggestionItem : list.getSuggestions()) {
                            suggestions.add(new MultiWordSuggestion(suggestionItem.getSuggestionText(), suggestionItem.getSuggestionText()));
                            allSuggestions.put(suggestionItem.getSuggestionText(), suggestionItem.getId());
                        }
                        Response response = new Response();
                        response.setSuggestions(suggestions);
                        callback.onSuggestionsReady(request, response);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("something going wrong while obtaining suggestions for '" + request.getQuery() + "'");
                    }
                });
                GWT.log("suggestion requested " + request.getQuery());
            }
        };
        final SuggestBox suggestBox = new SuggestBox(oracle);
        final HorizontalPanel selectedRecords = new HorizontalPanel();

        container.add(suggestBox);
        container.add(selectedRecords);

        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                final String value = event.getSelectedItem().getReplacementString();
                selectedSuggestions.put(value, allSuggestions.get(value));
                final HorizontalPanel record = new HorizontalPanel();
                record.add(new Label(value));
                final Button closeButton = new Button("X");
                closeButton.getElement().setId(allSuggestions.get(value).toString());
                closeButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        record.removeFromParent();
                        selectedSuggestions.remove(value);
                    }
                });
                record.add(closeButton);
                selectedRecords.add(record);
            }
        });
        return container;
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
