package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.MultiWordIdentifiableSuggestion;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipWidget;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EMPTY_VALUE;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends TooltipWidget implements HyperlinkStateChangedEventHandler {
    private static final String ALL_SUGGESTIONS = "*";
    private static final int HEIGHT_OFFSET = 20;
    private SuggestBox suggestBox;
    private LinkedHashMap<Id, String> stateListValues = new LinkedHashMap<>(); //used for temporary state
    private List<MultiWordIdentifiableSuggestion> suggestions = new ArrayList<MultiWordIdentifiableSuggestion>();
    private SuggestBoxConfig suggestBoxConfig;
    private LazyLoadState lazyLoadState;
    private int lastScrollPos;

    public SuggestBoxWidget() {

    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        initialData = currentState;
        final SuggestBoxState suggestBoxState = (SuggestBoxState) currentState;
        suggestBoxConfig = suggestBoxState.getSuggestBoxConfig();

        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.initModel(suggestBoxState);
        }
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
            timer.scheduleRepeating(100);
        }
    }

    @Override
    public Component createNew() {
        return new SuggestBoxWidget();
    }

    @Override
    protected boolean isChanged() {
        final Map<Id, String> initialValue = ((SuggestBoxState) getInitialData()).getListValues();
        final Map<Id, String> currentValue = ((SuggestPresenter) impl).selectedSuggestions;
        return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
    }

    @Override
    protected SuggestBoxState createNewState() {
        SuggestBoxState state = new SuggestBoxState();
        final SuggestBoxState initialState = getInitialData();
        state.setSelectedIds(initialState.getSelectedIds());
        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        if (!isEditable()) {
            return super.getFullClientStateCopy();
        }
        SuggestBoxState initialState = getInitialData();
        SuggestBoxState state = new SuggestBoxState();
        state.setListValues(stateListValues);

        state.setSelectedIds(initialState.getSelectedIds());
        state.getListValues().putAll(initialState.getListValues());
        state.setSingleChoice(initialState.isSingleChoice());
        state.setSuggestBoxConfig(initialState.getSuggestBoxConfig());
        state.setWidgetProperties(initialState.getWidgetProperties());
        state.setConstraints(initialState.getConstraints());
        return state;

    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        SuggestBoxState suggestBoxState = (SuggestBoxState) state;
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        SelectionStyleConfig selectionStyleConfig = suggestBoxState.getSuggestBoxConfig().getSelectionStyleConfig();
        HyperlinkNoneEditablePanel noneEditablePanel = new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus);
        return noneEditablePanel;
    }

    @Override
    public void onHyperlinkStateChangedEvent(HyperlinkStateChangedEvent event) {
        PopupPanel tooltip = event.getPopupPanel();
        if (tooltip != null) {
            tooltip.hide();
            fetchWidgetItems();
            return;
        }
        Id id = event.getId();
        List<Id> ids = new ArrayList<Id>();
        ids.add(id);
        RepresentationRequest request = new RepresentationRequest(ids,
                suggestBoxConfig.getSelectionPatternConfig().getValue(), suggestBoxConfig.getFormattingConfig());
        Command command = new Command("getRepresentationForOneItem", "representation-updater", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();

                stateListValues.put(id, representation);
                SuggestBoxState state = createNewState();
                setCurrentState(state);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink");
            }
        });

    }

    @Override
    protected String getTooltipHandlerName() {
        return "widget-items-handler";
    }

    @Override
    protected Widget asEditableWidget(final WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        final SuggestPresenter presenter = new SuggestPresenter();
        MultiWordSuggestOracle oracle = new Cm5MultiWordSuggestOracle();
        SuggestBoxDisplay display = new SuggestBoxDisplay();
        suggestBox = new SuggestBox(oracle, new TextBox(), display);
        presenter.suggestBox = suggestBox;
        display.setLazyLoadHandler(new ScrollLazyLoadHandler());
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                final MultiWordIdentifiableSuggestion selectedItem =
                        (MultiWordIdentifiableSuggestion) event.getSelectedItem();
                final String replacementString = selectedItem.getReplacementString();
                Id id = selectedItem.getId();
                presenter.insert(id, replacementString);
                stateListValues.put(id, replacementString);
                final SuggestBoxState suggestBoxState = SuggestBoxWidget.this.getInitialData();
                if (suggestBoxState.isSingleChoice()) {
                    suggestBoxState.getSelectedIds().clear();
                }
                suggestBoxState.getSelectedIds().add(id);
                suggestBox.refreshSuggestionList();
                SuggestBox sourceObject = (SuggestBox) event.getSource();
                sourceObject.setText(EMPTY_VALUE);
                presenter.changeLastElementHighlightingTo(false);
                sourceObject.setFocus(true);

            }
        });
        Event.sinkEvents(suggestBox.getElement(), Event.ONBLUR);
        suggestBox.addHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                validate();
                suggestBox.setText(EMPTY_VALUE);
            }
        }, BlurEvent.getType());
        suggestBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                int eventKeyCode = event.getNativeEvent().getKeyCode();
                switch (eventKeyCode) {
                    case KeyCodes.KEY_ENTER:
                        presenter.getNotFilteredSuggestions();
                        break;
                }
            }
        });

        suggestBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                int eventKeyCode = event.getNativeEvent().getKeyCode();
                switch (eventKeyCode) {
                    case KeyCodes.KEY_BACKSPACE:
                        presenter.handleBackspaceDown();
                        break;
                    case KeyCodes.KEY_ESCAPE:
                        presenter.resetUserInteraction();
                        break;
                    default:
                        presenter.changeLastElementHighlightingTo(false);
                }

            }
        });

        display.setPositionRelativeTo(presenter);
        return presenter;
    }

    private void initState(final SuggestBoxState state, final SuggestBox suggestBox) {
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.initView(state, suggestBox);
        } else {
            LinkedHashMap<Id, String> listValues = state.getListValues();
            HyperlinkNoneEditablePanel noneEditablePanel = (HyperlinkNoneEditablePanel) impl;
            noneEditablePanel.cleanPanel();
            if (isDisplayingAsHyperlink()) {
                noneEditablePanel.displayHyperlinks(listValues);
            } else {
                noneEditablePanel.displayItems(listValues.values());

            }
            if (shouldDrawTooltipButton()) {
                noneEditablePanel.addShowTooltipLabel(new ShowTooltipHandler());
            }
        }
    }

    private SuggestionRequest createSuggestionRequest(String requestQuery) {
        SuggestionRequest result = new SuggestionRequest();
        String name = suggestBoxConfig.getCollectionRefConfig().getName();
        result.setCollectionName(name);
        String dropDownPatternConfig = suggestBoxConfig.getDropdownPatternConfig().getValue();
        result.setDropdownPattern(dropDownPatternConfig);
        result.setSelectionPattern(suggestBoxConfig.getSelectionPatternConfig().getValue());
        result.setText(requestQuery);
        final SuggestPresenter presenter = (SuggestPresenter) impl;
        result.setExcludeIds(new LinkedHashSet<Id>(presenter.getSelectedKeys()));
        result.setInputTextFilterName(suggestBoxConfig.getInputTextFilterConfig().getName());
        result.setDefaultSortCriteriaConfig(suggestBoxConfig.getDefaultSortCriteriaConfig());
        result.setFormattingConfig(suggestBoxConfig.getFormattingConfig());
        if (lazyLoadState == null) {
            lazyLoadState = new LazyLoadState(suggestBoxConfig.getPageSize(), 0);
        }
        result.setLazyLoadState(lazyLoadState);
        return result;

    }

    private class SuggestPresenter extends CellPanel {

        private final Map<Id, String> selectedSuggestions;
        private boolean singleChoice;
        private Element container;
        private Element arrowBtn;
        private Element clearAllButton;
        private SuggestBox suggestBox;
        private boolean lastElementWasHighlighted;

        private SuggestPresenter() {
            Element row = DOM.createTR();
            this.selectedSuggestions = new HashMap<>();
            setStyleName("suggest-container-block");
            container = DOM.createTD();
            DOM.appendChild(row, container);
            DOM.appendChild(getBody(), row);
            arrowBtn = DOM.createTD();
            arrowBtn.setClassName("suggest-container-arrow-btn");
            DOM.setEventListener(arrowBtn, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    getNotFilteredSuggestions();
                }
            });

            DOM.appendChild(row, arrowBtn);
            clearAllButton = DOM.createTD();
            clearAllButton.setClassName("suggest-container-clear-btn");
            DOM.appendChild(row, clearAllButton);
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

        private void handleBackspaceDown() {
            if (!suggestBox.getText().equalsIgnoreCase(EMPTY_VALUE)) {
                return;
            }
            if (lastElementWasHighlighted) {
                SelectedItemComposite lastSelectionItem = getLastItem();
                if (lastSelectionItem != null) {
                    lastSelectionItem.removeFromParent();
                    Id id = lastSelectionItem.getItemId();
                    removeSuggestBoxIdFromStates(id);
                }
            }

            changeLastElementHighlightingTo(true);
        }

        private void resetUserInteraction() {
            ((SuggestBoxDisplay) suggestBox.getSuggestionDisplay()).hideSuggestions();
            suggestBox.setText(EMPTY_VALUE);
            changeLastElementHighlightingTo(false);
        }

        private void changeLastElementHighlightingTo(boolean wasHighlighted) {
            SelectedItemComposite lastSelectionItem = getLastItem();
            if (lastSelectionItem != null) {
                String styleName = wasHighlighted ? "highlightedFacebookElement" : "facebook-element";
                lastSelectionItem.setStyleName(styleName);
                lastElementWasHighlighted = wasHighlighted;
            }

        }

        private void removeSuggestBoxIdFromStates(Id id) {
            selectedSuggestions.remove(id);
            stateListValues.remove(id);
            SuggestBoxState suggestBoxState = getInitialData();
            suggestBoxState.getSelectedIds().remove(id);
            suggestBox.setFocus(true);
        }

        public void getNotFilteredSuggestions() {
            suggestBox.setText(ALL_SUGGESTIONS);
            changeSuggestionsPopupSize();
            lastScrollPos = 0;
            SuggestBoxDisplay display = (SuggestBoxDisplay) suggestBox.getSuggestionDisplay();
            if (!display.getSuggestionPopup().isShowing()) {
                lazyLoadState = null;
                suggestBox.showSuggestionList();
            }

            suggestBox.setText(EMPTY_VALUE);
            suggestBox.setFocus(true);

        }

        private void changeSuggestionsPopupSize() {
            SuggestBoxDisplay display = (SuggestBoxDisplay) suggestBox.getSuggestionDisplay();
            Style popupStyle = display.getSuggestionPopup().getElement().getStyle();
            popupStyle.setWidth(container.getOffsetWidth(), Style.Unit.PX);
            display.setLazyLoadPanelHeight(getSuggestionsAvailableHeight());
        }

        public int getSuggestionsAvailableHeight() {
            return Window.getClientHeight() - suggestBox.getAbsoluteTop() - suggestBox.getOffsetHeight() - HEIGHT_OFFSET;
        }

        public Set<Id> getSelectedKeys() {
            return selectedSuggestions.keySet();
        }

        public void initModel(final SuggestBoxState state) {
            selectedSuggestions.clear();
            final HashMap<Id, String> listValues = state.getListValues();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
            }
        }

        public void initView(final SuggestBoxState state, final SuggestBox suggestBox) {

            this.singleChoice = state.isSingleChoice();
            clear();
            this.suggestBox = suggestBox;
            container.setClassName("suggest-container-input");
            for (final Map.Entry<Id, String> listEntry : selectedSuggestions.entrySet()) {
                final SelectedItemComposite itemComposite =
                        new SelectedItemComposite(listEntry.getKey(), listEntry.getValue());
                itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
                if (isDisplayingAsHyperlink()) {
                    itemComposite.setHyperlinkListener(createHyperlinkListener(itemComposite));
                }
                super.add(itemComposite, container);
            }
            if (shouldDrawTooltipButton()) {
                Button openTooltip = new Button("..");
                openTooltip.setStyleName("light-button");
                openTooltip.addClickHandler(new ShowTooltipHandler());
                super.add(openTooltip, container);
            }

            super.add(suggestBox, container);
            if (state.getSuggestBoxConfig().getClearAllButtonConfig() != null) {
                FocusPanel focusPanel = new FocusPanel();
                ButtonForm clearButton = new ButtonForm(focusPanel,
                        state.getSuggestBoxConfig().getClearAllButtonConfig().getImage(),
                        state.getSuggestBoxConfig().getClearAllButtonConfig().getText());
                focusPanel.add(clearButton);
                super.add(focusPanel, clearAllButton);
                focusPanel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        stateListValues.clear();
                        selectedSuggestions.clear();
                        SuggestBoxState suggestBoxState = getInitialData();
                        suggestBoxState.getSelectedIds().clear();
                        clearAllItems();
                    }
                });
            }
            SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.changeSuggestionsPopupSize();
        }

        public void clearAllItems() {
            for (Iterator<Widget> it = getChildren().iterator(); it.hasNext(); ) {
                final Widget widget = it.next();
                if (widget instanceof SelectedItemComposite) {
                    it.remove();
                }
            }
        }

        public SelectedItemComposite getLastItem() {
            SelectedItemComposite result = null;
            WidgetCollection collection = getChildren();
            int lastElementIndex = collection.size() - 1;
            for (int i = lastElementIndex; i >= 0; i--) {
                Widget widget = collection.get(i);
                if (widget instanceof SelectedItemComposite) {
                    result = (SelectedItemComposite) widget;
                    break;
                }
            }
            return result;

        }

        public void insert(final Id itemId, final String itemName) {
            final SelectedItemComposite itemComposite = new SelectedItemComposite(itemId, itemName);
            itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
            if (isDisplayingAsHyperlink()) {
                itemComposite.setHyperlinkListener(createHyperlinkListener(itemComposite));
            }
            if (singleChoice) {
                selectedSuggestions.clear();
                stateListValues.clear();
                clearAllItems();
            }
            selectedSuggestions.put(itemId, itemName);
            int index = shouldDrawTooltipButton() ? container.getChildCount() - 2 : container.getChildCount() - 1;
            super.insert(itemComposite, container, index, true);

        }

        private EventListener createCloseBtnListener(final SelectedItemComposite itemComposite) {
            return new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    remove(itemComposite);
                    Id id = itemComposite.getItemId();
                    removeSuggestBoxIdFromStates(id);

                }
            };
        }

        private EventListener createHyperlinkListener(final SelectedItemComposite itemComposite) {
            return new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    HyperlinkClickHandler clickHandler = new HyperlinkClickHandler(itemComposite.getItemId(),
                            null, localEventBus);
                    clickHandler.onClick();
                }
            };
        }

    }

    private class SelectedItemComposite extends Composite {
        private final SimplePanel wrapper;
        private final Element closeBtn;
        private final Id itemId;
        private Element label;

        private SelectedItemComposite(final Id itemId, final String itemName) {
            this.itemId = itemId;
            wrapper = new SimplePanel();
            wrapper.setStyleName("facebook-element");
            label = DOM.createSpan();
            label.setInnerText(itemName);
            label.addClassName("facebook-label");
            label.addClassName("facebook-none-clickable-label");
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

        public void setHyperlinkListener(EventListener listener) {
            label.getStyle().setCursor(Style.Cursor.POINTER);
            DOM.setEventListener(label, listener);
            DOM.sinkEvents(label, Event.ONCLICK);

        }
    }

    class ScrollLazyLoadHandler implements ScrollHandler {
        private ScrollPanel lazyLoadPanel;

        public void setLazyLoadPanel(ScrollPanel lazyLoadPanel) {
            this.lazyLoadPanel = lazyLoadPanel;
        }

        @Override
        public void onScroll(ScrollEvent event) {

            int oldScrollPos = lastScrollPos;
            lastScrollPos = lazyLoadPanel.getVerticalScrollPosition();
            int maxScrollTop = lazyLoadPanel.getMaximumVerticalScrollPosition();
            // If scrolling up, ignore the event.
            if (oldScrollPos == maxScrollTop) {

            }
            if (oldScrollPos >= lastScrollPos) {
                return;
            }

            if (lastScrollPos >= maxScrollTop) {
                if (suggestBox.getText().isEmpty()) {
                    suggestBox.setText(ALL_SUGGESTIONS);
                }

                lazyLoadState.onNextPage();

                suggestBox.showSuggestionList();
                suggestBox.setText(EMPTY_VALUE);

            }
        }

    }

    private class Cm5MultiWordSuggestOracle extends MultiWordSuggestOracle {
        @Override
        public void requestSuggestions(final Request request, final Callback callback) {
            final String requestQuery = request.getQuery();
            if (ALL_SUGGESTIONS.equalsIgnoreCase(requestQuery)) {
                fetchSuggestions(requestQuery, request, callback);
            } else {
                Timer timer = new Timer() {
                    @Override
                    public void run() {
                        String filterText = suggestBox.getText();
                        if (requestQuery.equalsIgnoreCase(filterText)) {
                            fetchSuggestions(requestQuery, request, callback);
                        }
                        this.cancel();
                    }
                };
                timer.scheduleRepeating(500);
            }
        }

    }

    private void fetchSuggestions(String requestQuery, final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
        SuggestionRequest suggestionRequest = createSuggestionRequest(requestQuery);
        Command command = new Command("obtainSuggestions", SuggestBoxWidget.this.getName(), suggestionRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                SuggestionList suggestionResponse = (SuggestionList) result;

                boolean isResponseForMoreItems = suggestionResponse.isResponseForMoreItems();
                if (!isResponseForMoreItems) {
                    suggestions.clear();

                }
                List<SuggestionItem> suggestionItems = suggestionResponse.getSuggestions();
                if (suggestionItems.isEmpty()) {
                    lazyLoadState = null;
                }
                for (SuggestionItem suggestionItem : suggestionResponse.getSuggestions()) {
                    suggestions.add(new MultiWordIdentifiableSuggestion(suggestionItem.getId(),
                            suggestionItem.getReplacementText(), suggestionItem.getDisplayText()));
                }
                SuggestOracle.Response response = new SuggestOracle.Response();
                response.setSuggestions(suggestions);
                callback.onSuggestionsReady(request, response);
                ((SuggestBoxDisplay) suggestBox.getSuggestionDisplay())
                        .setScrollPosition(lastScrollPos);
            }

            @Override
            public void onFailure(Throwable caught) {

                GWT.log("something was going wrong while obtaining suggestions for '" + request.getQuery() + "'");
            }
        });
        GWT.log("suggestion requested " + request.getQuery());
    }

}
