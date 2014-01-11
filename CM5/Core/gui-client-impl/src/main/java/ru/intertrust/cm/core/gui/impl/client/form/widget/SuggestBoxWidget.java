package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.*;
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

import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends BaseWidget {

    private SuggestBox suggestBox;
    private SuggestBoxState currentState;
    private SuggestPresenter presenter;
    private final HashMap<Id, String> allSuggestions = new HashMap<Id, String>();

    public SuggestBoxWidget() {
        presenter = new SuggestPresenter(true);
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        final SuggestBoxState suggestBoxState = (SuggestBoxState) currentState;
        this.currentState = suggestBoxState;
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (presenter.getOffsetWidth() > 0) {
                    presenter.init(suggestBoxState, suggestBox);
                    this.cancel();
                }
            }
        };
        timer.scheduleRepeating(500);
    }

    @Override
    public Component createNew() {
        return new SuggestBoxWidget();
    }

    @Override
    public SuggestBoxState getCurrentState() {
        SuggestBoxState state = new SuggestBoxState();
        state.setSelectedIds(new ArrayList<Id>(presenter.getSelectedKys()));
        return state;
    }

    @Override
    protected Widget asNonEditableWidget() {
        return presenter;
    }

    @Override
    protected Widget asEditableWidget() {
        presenter.setArrowBtnListener(new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                suggestBox.setText("*");
                suggestBox.showSuggestionList();
                suggestBox.setText("");
            }
        });

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
                suggestionRequest.setExcludeIds(new LinkedHashSet<Id>(presenter.getSelectedKys()));
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
        private final Element container;
        private Element arrowBtn;
        private SuggestBox suggestBox;

        private SuggestPresenter(final boolean editable) {
            this.selectedSuggestions = new HashMap<Id, String>();
            setStyleName("suggest-container");
            final Element row = DOM.createTR();
            container = DOM.createTD();
            DOM.appendChild(row, container);
            DOM.appendChild(getBody(), row);
            if (editable) {
                arrowBtn = DOM.createTD();
                arrowBtn.setClassName("arrow-suggest-btn");
                DOM.appendChild(row, arrowBtn);
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
        }

        public Set<Id> getSelectedKys() {
            return selectedSuggestions.keySet();
        }

        public void setArrowBtnListener(final EventListener listener) {
            // NPE not checked, for developers only
            DOM.setEventListener(arrowBtn, listener);
            DOM.sinkEvents(arrowBtn, Event.ONCLICK);
        }

        public void init(final SuggestBoxState state, final SuggestBox suggestBox) {
            this.suggestBox = suggestBox;
            getElement().getStyle().setProperty("maxWidth", getContainerWidth(), Style.Unit.PX);
            container.getStyle().setWidth(100, Style.Unit.PCT);
            final HashMap<Id, String> listValues = state.getObjects();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                final SelectedItemComposite itemComposite =
                        new SelectedItemComposite(listEntry.getKey(), listEntry.getValue(), state.isEditable());
                itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
                super.add(itemComposite, container);
                selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
            }
            if (state.isEditable()) {
                super.add(suggestBox, container);
                updateSuggestBoxWidth();
            }
        }

        public void insert(final Id itemId, final String itemName) {
            final SelectedItemComposite itemComposite = new SelectedItemComposite(itemId, itemName, true);
            itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
            selectedSuggestions.put(itemId, itemName);
            final int index = container.getChildCount() - 1;
            super.insert(itemComposite, container, index, true);
            updateSuggestBoxWidth();
        }

        private void updateSuggestBoxWidth() {
            final int parentWidth = getContainerWidth() - 22;
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

        private int getContainerWidth() {
            final int clientWidth = getElement().getClientWidth() - 4;
            return arrowBtn == null ? clientWidth : clientWidth - arrowBtn.getOffsetWidth();
        }
    }

    private static class SelectedItemComposite extends Composite {
        private final SimplePanel wrapper;
        private final Element closeBtn;
        private final Id itemId;

        private SelectedItemComposite(final Id itemId, final String itemName, final boolean editable) {
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
