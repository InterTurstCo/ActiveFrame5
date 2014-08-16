package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
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
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends TooltipWidget implements HyperlinkStateChangedEventHandler {

    private SuggestBox suggestBox;
    private String selectionPattern;
    private final HashMap<Id, String> allSuggestions = new HashMap<Id, String>();
    private LinkedHashMap<Id, String> stateListValues = new LinkedHashMap<>(); //used for temporary state
    private SuggestBoxConfig suggestBoxConfig;

    CmjDefaultSuggestionDisplay display;

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
        commonInitialization(suggestBoxState);
        SelectionStyleConfig selectionStyleConfig = suggestBoxState.getSuggestBoxConfig().getSelectionStyleConfig();
        HyperlinkNoneEditablePanel noneEditablePanel = new HyperlinkNoneEditablePanel(selectionStyleConfig, localEventBus);
        return noneEditablePanel;
    }

    private void commonInitialization(SuggestBoxState state) {
        SuggestBoxConfig config = state.getSuggestBoxConfig();
        selectionPattern = config.getSelectionPatternConfig().getValue();
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
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
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, suggestBoxConfig.getFormattingConfig());
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

    public class CmjDefaultSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
        public CmjDefaultSuggestionDisplay() {
            setSuggestionListHiddenWhenEmpty(false);

        }
        public PopupPanel getSuggestionPopup() {
            return this.getPopupPanel();
        }

    }

    @Override
    protected Widget asEditableWidget(final WidgetState state) {
        commonInitialization((SuggestBoxState) state);
        final SuggestPresenter presenter = new SuggestPresenter();
        MultiWordSuggestOracle oracle = buildDynamicMultiWordOracle();
        final CmjDefaultSuggestionDisplay defaultSuggestionDisplay = new CmjDefaultSuggestionDisplay();
        suggestBox = new SuggestBox(oracle, new TextBox(), defaultSuggestionDisplay);
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
                SuggestBox sourceObject = (SuggestBox) event.getSource();
                sourceObject.setText("");
                sourceObject.setFocus(true);
            }
        });
        Event.sinkEvents(suggestBox.getElement(), Event.ONBLUR);
        final PopupPanel suggestPopupPanel = defaultSuggestionDisplay.getSuggestionPopup();
               suggestPopupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {

              if(event.isAutoClosed()){
                  suggestBox.setText(BusinessUniverseConstants.EMPTY_VALUE);
              }
            }
        });

        suggestBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    if(!suggestPopupPanel.isShowing()) {
                        suggestBox.setText(BusinessUniverseConstants.EMPTY_VALUE);
                    }
                }
            }
        });

        display = (CmjDefaultSuggestionDisplay) suggestBox.getSuggestionDisplay();
        display.setPositionRelativeTo(presenter);
        display.getSuggestionPopup().getElement().getStyle().setZIndex(999999999);
        presenter.suggestBox = suggestBox;
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
                suggestionRequest.setDefaultSortCriteriaConfig(suggestBoxConfig.getDefaultSortCriteriaConfig());
                suggestionRequest.setFormattingConfig(suggestBoxConfig.getFormattingConfig());
                Command command = new Command("obtainSuggestions", SuggestBoxWidget.this.getName(), suggestionRequest);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onSuccess(Dto result) {
                        SuggestionList list = (SuggestionList) result;
                        ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
                        allSuggestions.clear();
                        for (SuggestionItem suggestionItem : list.getSuggestions()) {
                            suggestions.add(new MultiWordIdentifiableSuggestion(suggestionItem.getId(),
                                    suggestionItem.getReplacementText(), suggestionItem.getDisplayText()));
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

    private class SuggestPresenter extends CellPanel {

        private final Map<Id, String> selectedSuggestions;
        private boolean singleChoice;
        private Element container;
        private Element arrowBtn;
        private Element clearAllButton;
        private SuggestBox suggestBox;
        private Integer maxDropDownWidth;
        private Integer maxDropDownHeight;

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
                    final CmjDefaultSuggestionDisplay display = (CmjDefaultSuggestionDisplay) suggestBox.getSuggestionDisplay();
                    Element e = (Element) display.getSuggestionPopup().getElement().getFirstChild().getFirstChild().
                            getFirstChild().getChild(1).getChild(1).getFirstChild();
                    suggestBox.setText("*");
                    //max-width drop down suggest
                    if (getMaxDropDownWidth() != null) {
                        e.getStyle().setWidth(getMaxDropDownWidth(), Style.Unit.PX);
                    } else {
                        e.getStyle().setWidth((Window.getClientWidth() - 15) - suggestBox.getAbsoluteLeft(), Style.Unit.PX);
                    }
                    //end max-width drop down suggest

                    //max-height drop down suggest

                    if (getMaxDropDownHeight() != null) {
                        e.getStyle().setHeight(getMaxDropDownHeight(), Style.Unit.PX);
                        e.getStyle().setOverflowY(Style.Overflow.SCROLL);
                    } else {
                        suggestBox.showSuggestionList();
                        e.getStyle().setHeight(Window.getClientHeight() - suggestBox.getAbsoluteTop() - suggestBox.getOffsetHeight() - 25, Style.Unit.PX);
                        e.getStyle().setOverflowY(Style.Overflow.SCROLL);
                    }

                    //end max-height drop down suggest
                    if (!((CmjDefaultSuggestionDisplay) suggestBox.getSuggestionDisplay()).getSuggestionPopup().isShowing()) {
                        suggestBox.showSuggestionList();
                    }

                    suggestBox.setText("");
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

        public Set<Id> getSelectedKeys() {
            return selectedSuggestions.keySet();
        }

        private Integer getMaxDropDownWidth() {
            return maxDropDownWidth;
        }

        private Integer getMaxDropDownHeight() {
            return maxDropDownHeight;
        }

        public void initModel(final SuggestBoxState state) {
            selectedSuggestions.clear();
            final HashMap<Id, String> listValues = state.getListValues();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
            }
        }

        public void initView(final SuggestBoxState state, final SuggestBox suggestBox) {
            if (state.getSuggestBoxConfig().getMaxDropDownWidth() != null) {
                this.maxDropDownWidth = state.getSuggestBoxConfig().getMaxDropDownWidth();
            }
            if (state.getSuggestBoxConfig().getMaxDropDownHeight() != null) {
                this.maxDropDownHeight = state.getSuggestBoxConfig().getMaxDropDownHeight();
            }

            this.singleChoice = state.isSingleChoice();
            clear();
            this.suggestBox = suggestBox;
            if (getElement().getStyle().getProperty("maxWidth").isEmpty()) {
                container.setClassName("suggest-container-input");
            }
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
        }

        public void clearAllItems() {
            for (Iterator<Widget> it = getChildren().iterator(); it.hasNext(); ) {
                final Widget widget = it.next();
                if (widget instanceof SelectedItemComposite) {
                    it.remove();
                }
            }
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
                    selectedSuggestions.remove(id);
                    stateListValues.remove(id);
                    SuggestBoxState suggestBoxState = getInitialData();
                    suggestBoxState.getSelectedIds().remove(id);
                    suggestBox.setFocus(true);

                }
            };
        }

        private EventListener createHyperlinkListener(final SelectedItemComposite itemComposite) {
            return new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    HyperlinkClickHandler clickHandler = new HyperlinkClickHandler(itemComposite.getItemId(), null, localEventBus);
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
}
