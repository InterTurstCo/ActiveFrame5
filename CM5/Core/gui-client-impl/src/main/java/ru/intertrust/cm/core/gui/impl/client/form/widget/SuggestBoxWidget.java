package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
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

    private SuggestBox suggestBox;
    private final HashMap<Id, String> allSuggestions = new HashMap<Id, String>();

    public SuggestBoxWidget() {
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        final SuggestBoxState suggestBoxState = (SuggestBoxState) currentState;
        if (impl.getOffsetWidth() > 0) {
            initState(suggestBoxState, suggestBox);
        } else {
            final Timer timer = new Timer() {
                @Override
                public void run() {
                    if (impl.getOffsetWidth() > 0) {
                        initState(suggestBoxState, suggestBox);
                        this.cancel();
                    }
                }
            };
            timer.scheduleRepeating(500);
        }
    }

    @Override
    public Component createNew() {
        return new SuggestBoxWidget();
    }

    @Override
    public SuggestBoxState getCurrentState() {
        SuggestBoxState state = new SuggestBoxState();
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            state.setSelectedIds(new ArrayList<Id>(presenter.getSelectedKeys()));
        } else {
            final SuggestBoxState initialState = getInitialData();
            state.setSelectedIds(initialState.getSelectedIds());
        }
        return state;
    }

    @Override
    protected Widget asNonEditableWidget() {
        final Label label = new Label();
        label.setStyleName("suggest-choose-lbl");
        return label;
    }

    @Override
    protected Widget asEditableWidget() {
        final SuggestPresenter presenter = new SuggestPresenter();
        MultiWordSuggestOracle oracle = buildDynamicMultiWordOracle();
        suggestBox = new SuggestBox(oracle);
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                final MultiWordIdentifiableSuggestion selectedItem =
                        (MultiWordIdentifiableSuggestion) event.getSelectedItem();
                final String replacementString = selectedItem.getReplacementString();
                presenter.insert(selectedItem.getId(), replacementString);
                SuggestBox sourceObject = (SuggestBox) event.getSource();
                sourceObject.setText("");
                sourceObject.setFocus(true);
            }
        });
        return presenter;
    }

    private void initState(final SuggestBoxState state, final SuggestBox suggestBox) {
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;

            presenter.init(state, suggestBox);
        } else {
            final int maxWidth = impl.getElement().getParentElement().getClientWidth() - 4;
            impl.getElement().getStyle().setProperty("maxWidth", maxWidth, Style.Unit.PX);
            final StringBuilder builder = new StringBuilder();
            final HashMap<Id, String> listValues = state.getObjects();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                builder.append(listEntry.getValue()).append(", ");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 2);
            }
            final Label label = (Label) impl;
            label.setText(builder.toString());
        }
    }

    private MultiWordSuggestOracle buildDynamicMultiWordOracle() {
        return new MultiWordSuggestOracle() {
            @Override
            public void requestSuggestions(final Request request, final Callback callback) {
                SuggestionRequest suggestionRequest = new SuggestionRequest();
                final SuggestBoxState state = getInitialData();
                SuggestBoxConfig suggestBoxConfig = state.getSuggestBoxConfig();
                String name = suggestBoxConfig.getCollectionRefConfig().getName();
                suggestionRequest.setCollectionName(name);
                String dropDownPatternConfig = suggestBoxConfig.getDropdownPatternConfig().getValue();

                suggestionRequest.setDropdownPattern(dropDownPatternConfig);
                suggestionRequest.setSelectionPattern(suggestBoxConfig.getSelectionPatternConfig().getValue());
                suggestionRequest.setText(request.getQuery());
                final SuggestPresenter presenter = (SuggestPresenter) impl;
                suggestionRequest.setExcludeIds(new LinkedHashSet<Id>(presenter.getSelectedKeys()));
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

    private static class SuggestPresenter extends CellPanel {

        private final Map<Id, String> selectedSuggestions;
        private Element container;
        private Element arrowBtn;
        private SuggestBox suggestBox;

        private SuggestPresenter() {
            this.selectedSuggestions = new HashMap<Id, String>();
            setStyleName("suggest-container");
            final Element row = DOM.createTR();
            container = DOM.createTD();
            DOM.appendChild(row, container);
            DOM.appendChild(getBody(), row);
            arrowBtn = DOM.createTD();
            arrowBtn.setClassName("arrow-suggest-btn");
            DOM.appendChild(row, arrowBtn);
            DOM.setEventListener(arrowBtn, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    suggestBox.setText("*");
                    suggestBox.showSuggestionList();
                    suggestBox.setText("");
                }
            });
            DOM.sinkEvents(arrowBtn, Event.ONCLICK);
            DOM.setEventListener(container, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if (suggestBox != null) {
                        suggestBox.setFocus(true);
                    }
                }
            });
            DOM.sinkEvents(container, Event.ONCLICK | Event.ONFOCUS);
        }

        public Set<Id> getSelectedKeys() {
            return selectedSuggestions.keySet();
        }

        public void init(final SuggestBoxState state, final SuggestBox suggestBox) {
            clearContainer();
            this.suggestBox = suggestBox;
            if (getElement().getStyle().getProperty("maxWidth").isEmpty()) {
                final int maxWidth = getElement().getClientWidth() - arrowBtn.getOffsetWidth();
                getElement().getStyle().setProperty("maxWidth", maxWidth, Style.Unit.PX);
                container.getStyle().setWidth(100, Style.Unit.PCT);
            }
            final HashMap<Id, String> listValues = state.getObjects();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                final SelectedItemComposite itemComposite =
                        new SelectedItemComposite(listEntry.getKey(), listEntry.getValue());
                itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
                super.add(itemComposite, container);
                selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
            }
            super.add(suggestBox, container);
            updateSuggestBoxWidth();
        }

        public void clearContainer() {
            clear();
            selectedSuggestions.clear();
        }

        public void insert(final Id itemId, final String itemName) {
            final SelectedItemComposite itemComposite = new SelectedItemComposite(itemId, itemName);
            itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
            selectedSuggestions.put(itemId, itemName);
            final int index = container.getChildCount() - 1;
            super.insert(itemComposite, container, index, true);
            updateSuggestBoxWidth();
        }

        private void updateSuggestBoxWidth() {
            final int parentWidth = getElement().getClientWidth() - arrowBtn.getOffsetWidth() - 26;
            int childWidth = 0;
            for (int index = 0; index < getWidgetCount(); index++) {
                final Widget child = getWidget(index);
                if (child instanceof SelectedItemComposite) {
                    childWidth += child.getOffsetWidth();
                    if (childWidth > parentWidth) {
                        childWidth = child.getOffsetWidth();
                    }
                } else {
                    break;
                }
            }
            childWidth = parentWidth - childWidth;
            if (childWidth < 40) {
                childWidth = parentWidth;
            }
            suggestBox.getElement().getStyle().setWidth(childWidth, Style.Unit.PX);
        }

        private EventListener createCloseBtnListener(final SelectedItemComposite itemComposite) {
            return new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    remove(itemComposite);
                    selectedSuggestions.remove(itemComposite.getItemId());
                    suggestBox.setFocus(true);
                    updateSuggestBoxWidth();
                }
            };
        }
    }

    private static class SelectedItemComposite extends Composite {
        private final SimplePanel wrapper;
        private final Element closeBtn;
        private final Id itemId;

        private SelectedItemComposite(final Id itemId, final String itemName) {
            this.itemId = itemId;
            wrapper = new SimplePanel();
            wrapper.setStyleName("suggest-choose");
            final Element label = DOM.createSpan();
            label.setInnerText(itemName);
            DOM.appendChild(wrapper.getElement(), label);
            closeBtn = DOM.createSpan();
            closeBtn.setClassName("suggest-choose-close");
            DOM.appendChild(wrapper.getElement(), closeBtn);
            initWidget(wrapper);
        }

        public Id getItemId() {
            return itemId;
        }

        public void setCloseBtnListener(final EventListener listener) {
            DOM.setEventListener(closeBtn, listener);
            DOM.sinkEvents(closeBtn, Event.BUTTON_LEFT);
        }
    }
}
