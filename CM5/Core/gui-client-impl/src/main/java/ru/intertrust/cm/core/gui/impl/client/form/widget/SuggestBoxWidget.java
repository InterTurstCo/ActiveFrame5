package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.ComponentHelper;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HyperlinkStateChangedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.TooltipCallback;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkClickHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.linkediting.LinkCreatorWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.suggestbox.SuggestTextBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.MultiWordIdentifiableSuggestion;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipButtonClickHandler;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LazyLoadState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkCreatorWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionItem;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionList;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EMPTY_VALUE;
import static ru.intertrust.cm.core.gui.model.util.WidgetUtil.shouldDrawTooltipButton;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends LinkCreatorWidget implements HyperlinkStateChangedEventHandler,
        WidgetItemRemoveEventHandler {
    private static final String ALL_SUGGESTIONS = "*";
    private static final int HEIGHT_OFFSET_DOWN = 20;
    private static final int HEIGHT_OFFSET_UP = 20;
    private static final int INPUT_MARGIN = 35;
    private static final int ONE_SUGGESTION_HEIGHT = 18;
    private SuggestBox suggestBox;
    private List<MultiWordIdentifiableSuggestion> suggestions = new ArrayList<MultiWordIdentifiableSuggestion>();
    private SuggestBoxConfig suggestBoxConfig;
    private LazyLoadState lazyLoadState;
    private int lastScrollPos;
    private SuggestBoxState currentState;
    private Set<Id> initiallySelectedIds = new HashSet<>();


    @Override
    public void setCurrentState(WidgetState state) {
        currentState = (SuggestBoxState)state;
        initiallySelectedIds.clear();
        if (currentState.getSelectedIds() != null) {
            initiallySelectedIds.addAll(currentState.getSelectedIds());
        }

        suggestBoxConfig = currentState.getSuggestBoxConfig();
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.initModel(currentState);
        }
        if (impl.getOffsetWidth() > 0) {
            initState(currentState, suggestBox);
        } else {

            final Timer timer = new Timer() {
                @Override
                public void run() {
                    if (impl.getOffsetWidth() > 0) {
                        initState(currentState, suggestBox);
                        if (impl.getOffsetWidth() > 200) {
                            impl.setWidth(impl.getElement().getOffsetWidth() + "px");
                        }
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
        Set<Id> currentlySelectedIds = currentState.getSelectedIds();
        return currentlySelectedIds == null ? !initiallySelectedIds.isEmpty() : !currentlySelectedIds.equals(initiallySelectedIds);
    }

    @Override
    protected SuggestBoxState createNewState() {
        SuggestBoxState state = new SuggestBoxState();
        state.setSelectedIds(currentState.getSelectedIds());
        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        if (!isEditable()) {
            return super.getFullClientStateCopy();
        }
        SuggestBoxState state = new SuggestBoxState();
        state.setSelectedIds(currentState.getSelectedIds());
        state.setSingleChoice(currentState.isSingleChoice());
        state.setSuggestBoxConfig(currentState.getSuggestBoxConfig());
        state.setWidgetProperties(currentState.getWidgetProperties());
        state.setConstraints(currentState.getConstraints());
        return state;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        SuggestBoxState suggestBoxState = (SuggestBoxState) state;
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        localEventBus.addHandler(ShowTooltipEvent.TYPE, this);
        SelectionStyleConfig selectionStyleConfig = suggestBoxState.getSuggestBoxConfig().getSelectionStyleConfig();
        HyperlinkNoneEditablePanel noneEditablePanel = new HyperlinkNoneEditablePanel(selectionStyleConfig,
                localEventBus, false, suggestBoxState.getTypeTitleMap(), this);
        return noneEditablePanel;
    }

    @Override
    public void onHyperlinkStateChangedEvent(final HyperlinkStateChangedEvent event) {
        Id id = event.getId();
        List<Id> ids = new ArrayList<Id>();
        ids.add(id);
        String collectionName = suggestBoxConfig.getCollectionRefConfig() == null ? null
                : suggestBoxConfig.getCollectionRefConfig().getName();
        RepresentationRequest request = new RepresentationRequest(ids,
                suggestBoxConfig.getSelectionPatternConfig().getValue(), collectionName, suggestBoxConfig.getFormattingConfig());
        Command command = new Command("getRepresentationForOneItem", "representation-updater", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                HyperlinkDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
                boolean tooltipContent = event.isTooltipContent();
                LinkedHashMap<Id, String> listValues = tooltipContent ? currentState.getTooltipValues()
                        : currentState.getListValues();
                listValues.put(id, representation);
                if (hyperlinkDisplay != null) {

                    hyperlinkDisplay.displayHyperlinks(listValues, !tooltipContent && shouldDrawTooltipButton(currentState));
                } else {

                    currentState.getListValues().put(id, representation);
                    setCurrentState(currentState);
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });

    }

    @Override
    protected String getTooltipHandlerName() {
        return "widget-items-handler";
    }

    @Override
    protected void removeTooltipButton() {
        SuggestPresenter presenter = (SuggestPresenter) impl;
        presenter.getTooltipButton().removeFromParent();
    }

    @Override
    protected void drawItemFromTooltipContent() {
        SuggestPresenter presenter = (SuggestPresenter) impl;
        presenter.drawItemFromTooltipContent();
    }

    @Override
    protected Widget asEditableWidget(final WidgetState state) {
        localEventBus.addHandler(HyperlinkStateChangedEvent.TYPE, this);
        localEventBus.addHandler(ShowTooltipEvent.TYPE, this);
        localEventBus.addHandler(WidgetItemRemoveEvent.TYPE, this);
        final SuggestPresenter presenter = new SuggestPresenter((LinkCreatorWidgetState) state);
        MultiWordSuggestOracle oracle = new Cm5MultiWordSuggestOracle();
        final SuggestBoxDisplay display = new SuggestBoxDisplay();
        TextBox suggestTextBox = new SuggestTextBox();
        suggestBox = new SuggestBox(oracle, suggestTextBox, display);

        presenter.suggestBox = suggestBox;
        display.setLazyLoadHandler(new ScrollLazyLoadHandler());
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                final MultiWordIdentifiableSuggestion selectedItem =
                        (MultiWordIdentifiableSuggestion) event.getSelectedItem();
                Id id = selectedItem.getId();
                String representation = selectedItem.getReplacementString();
                insertItem(id, representation);
                suggestBox.refreshSuggestionList();

            }
        });
        Event.sinkEvents(suggestBox.getElement(), Event.ONBLUR);
        suggestBox.addHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                validate();
                if(display.isNotShown()){
                suggestBox.setText(EMPTY_VALUE);
                }
            }
        }, BlurEvent.getType());

        suggestBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                lazyLoadState = null; //all events cause suggest popup closing or reopening, so state should be reseted
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

    private void insertItem(Id id, String representation){
        SuggestPresenter presenter = (SuggestPresenter) impl;
        if (shouldDrawTooltipButton(currentState, 1)) {
            insertInTooltipContent(id, representation);
        } else {
            final String replacementString = representation;
            presenter.insert(id, replacementString);
            currentState.getListValues().put(id, replacementString);
            if (impl.getElement().getStyle().getWidth().equalsIgnoreCase(EMPTY_VALUE)) {
                impl.setWidth(impl.getElement().getOffsetWidth() + "px");
            }

        }
        currentState.getSelectedIds().add(id);
        presenter.changeSuggestInputWidth();
        suggestBox.setText(EMPTY_VALUE);
        suggestBox.setFocus(true);
        clearErrors();
    }

    @Override
    public Object getValue() {
        Map<Id, String> currentValue = ((SuggestPresenter) impl).selectedSuggestions;
        return new ArrayList(currentValue.values());
    }

    @Override
    public void showErrors(ValidationResult errors) {
        String errorString = StringUtil.join(getMessages(errors), "\n");
        if (impl.getTitle() != null) {
            errorString = impl.getTitle() + errorString;
        }
        impl.setTitle(errorString);
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.container.addClassName("validation-error");
            presenter.suggestBox.addStyleName("validation-error");

        }
    }

    @Override
    public void clearErrors() {
        impl.setTitle(null);
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.container.removeClassName("validation-error");
            presenter.suggestBox.removeStyleName("validation-error");
        }
    }

    private void insertInTooltipContent(final Id id, final String representation) {
        LinkedHashMap<Id, String> tooltipValues = currentState.getTooltipValues();
        if (tooltipValues == null) {
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    currentState.getTooltipValues().put(id, representation);
                }
            });
        } else {
            tooltipValues.put(id, representation);
        }

        SuggestPresenter presenter = (SuggestPresenter) impl;
        Widget tooltipButton = presenter.getTooltipButton();
        if (tooltipButton == null) {
            presenter.addTooltipButton(true);
        }
    }

    private void initState(final SuggestBoxState state, final SuggestBox suggestBox) {
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.initView(state, suggestBox);
        } else {
            LinkedHashMap<Id, String> listValues = state.getListValues();
            HyperlinkNoneEditablePanel noneEditablePanel = (HyperlinkNoneEditablePanel) impl;

            if (state.isDisplayingAsHyperlinks()) {
                noneEditablePanel.displayHyperlinks(listValues, shouldDrawTooltipButton(state));
            } else {
                noneEditablePanel.displayItems(listValues.values(), shouldDrawTooltipButton(state));

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
        result.setExcludeIds(new LinkedHashSet<Id>(currentState.getSelectedIds()));
        result.setComplicatedFiltersParams(createFiltersParams(requestQuery));
        result.setDefaultSortCriteriaConfig(suggestBoxConfig.getDefaultSortCriteriaConfig());
        result.setFormattingConfig(suggestBoxConfig.getFormattingConfig());
        result.setCollectionExtraFiltersConfig(suggestBoxConfig.getCollectionExtraFiltersConfig());
        if (lazyLoadState == null) {
            lazyLoadState = new LazyLoadState(suggestBoxConfig.getPageSize(), 0);
        }
        result.setLazyLoadState(lazyLoadState);
        return result;

    }
    private ComplicatedFiltersParams createFiltersParams(String requestQuery){
        Collection<WidgetIdComponentName> widgetsIdsComponentNames = currentState.getExtraWidgetIdsComponentNames();
        String filterName = suggestBoxConfig.getInputTextFilterConfig().getName();
        WidgetsContainer container = getContainer();
        return GuiUtil.createComplicatedFiltersParams(requestQuery, filterName, container, widgetsIdsComponentNames);
    }

    @Override
    public void onWidgetItemRemove(WidgetItemRemoveEvent event) {
        currentState.getSelectedIds().remove(event.getId());
        if (event.isTooltipContent()) {
            currentState.getTooltipValues().remove(event.getId());
        } else {
            currentState.getListValues().remove(event.getId());

        }
        SuggestPresenter presenter = (SuggestPresenter) impl;
        Widget tooltipButton = presenter.getTooltipButton();
        if (tooltipButton != null && !shouldDrawTooltipButton(currentState)) {
            tooltipButton.removeFromParent();
            presenter.changeSuggestInputWidth();
        }

    }

    @Override
    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return currentState.getWidgetConfig().getLinkedFormMappingConfig();
    }

    @Override
    public LinkedFormConfig getLinkedFormConfig() {
        return currentState.getWidgetConfig().getLinkedFormConfig();
    }

    @Override
    protected void handleNewCreatedItem(Id id, String representation) {
        insertItem(id, representation);
    }

    private class SuggestPresenter extends CellPanel {

        private final Map<Id, String> selectedSuggestions;

        private Element container;
        private Element arrowBtn;
        private Element clearAllButton;
        private SuggestBox suggestBox;
        private boolean lastElementWasHighlighted;

        private SuggestPresenter(LinkCreatorWidgetState state) {
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
            addCreateButton(state, row);
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
        private void addCreateButton(LinkCreatorWidgetState state, Element row){
            final ConfiguredButton button = getCreateButton(state);
            if(button != null){
                Element createButtonContainer = DOM.createTD();
                Element createButtonElement = button.getElement();
                DOM.appendChild(createButtonContainer, createButtonElement);
                DOM.appendChild(row, createButtonContainer);
                DOM.sinkEvents(createButtonElement, Event.ONCLICK);
                DOM.setEventListener(createButtonElement, new EventListener() {
                    @Override
                    public void onBrowserEvent(Event event) {
                        getClickAction().perform();
                    }
                });

            }
        }

        private void handleBackspaceDown() {
            if (!suggestBox.getText().equalsIgnoreCase(EMPTY_VALUE)) {
                return;
            }
            if (lastElementWasHighlighted) {
                SelectedItemComposite lastSelectionItem = getLastItem();
                if (lastSelectionItem != null) {
                    tryToPoolFromTooltipContent();
                    lastSelectionItem.removeFromParent();
                    Id id = lastSelectionItem.getItemId();
                    removeSuggestItemFromStates(id);
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

        private void removeSuggestItemFromStates(Id id) {
            selectedSuggestions.remove(id);
            currentState.getListValues().remove(id);
            currentState.getSelectedIds().remove(id);
            currentState.decrementFilteredItemsNumber();
            suggestBox.setFocus(true);
        }

        protected void drawItemFromTooltipContent() {
            Map.Entry<Id, String> entry = pollItemFromTooltipContent();
            currentState.getListValues().put(entry.getKey(), entry.getValue());
            SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.insert(entry.getKey(), entry.getValue());
            presenter.changeSuggestInputWidth();

        }


        public void getNotFilteredSuggestions() {
            suggestBox.setText(ALL_SUGGESTIONS);
            lastScrollPos = 0;
            lazyLoadState = null;
            suggestBox.showSuggestionList();
            suggestBox.setText(EMPTY_VALUE);
            suggestBox.setFocus(true);

        }

        private void changeSuggestionsPopupSize() {
            SuggestBoxDisplay display = (SuggestBoxDisplay) suggestBox.getSuggestionDisplay();
            Style popupStyle = display.getSuggestionPopup().getElement().getStyle();
            popupStyle.setWidth(container.getOffsetWidth(), Style.Unit.PX);
            int screenAvailableHeight = getScreenAvailableHeight();
            int suggestionsHeight = getSuggestionsHeight();
            int lazyLoadPanelHeight = screenAvailableHeight >= suggestionsHeight ? suggestionsHeight : screenAvailableHeight;
            display.setLazyLoadPanelHeight(lazyLoadPanelHeight);
        }

        public int getScreenAvailableHeight() {
            int fullHeight = Window.getClientHeight();
            Element center = DOM.getElementById(ComponentHelper.DOMAIN_ID);
            int domainObjectHeight = center == null ? HEIGHT_OFFSET_UP : center.getAbsoluteTop();
            int aboveSuggestBoxHeight = suggestBox.getAbsoluteTop() - domainObjectHeight;
            int belowSuggestBoxHeight = fullHeight - suggestBox.getAbsoluteTop() - suggestBox.getOffsetHeight() - HEIGHT_OFFSET_DOWN;
            return aboveSuggestBoxHeight >= belowSuggestBoxHeight ? aboveSuggestBoxHeight : belowSuggestBoxHeight;
        }

        public int getSuggestionsHeight() {
            int pageSize = currentState.getSuggestBoxConfig().getPageSize();
            int suggestionsSize = suggestions.size();
            boolean scrollable = suggestionsSize >= pageSize;
            int size = scrollable ? pageSize : suggestionsSize;
            int result = scrollable ? size * ONE_SUGGESTION_HEIGHT - 2 * HEIGHT_OFFSET_UP : size * ONE_SUGGESTION_HEIGHT;
            return result;
        }

        public void initModel(final SuggestBoxState state) {
            selectedSuggestions.clear();
            final HashMap<Id, String> listValues = state.getListValues();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
            }
        }

        public void initView(final SuggestBoxState state, final SuggestBox suggestBox) {

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
            if (shouldDrawTooltipButton(currentState)) {
                addTooltipButton(false);
            }
            addClearButton(state);
            super.add(suggestBox, container);
            changeSuggestInputWidth();

        }

        private void addClearButton(SuggestBoxState state){
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
                        currentState.getListValues().clear();
                        selectedSuggestions.clear();
                        currentState.evictTooltipItems();
                        currentState.getSelectedIds().clear();
                        clearAll();

                    }
                });
            }

        }

        private void addTooltipButton(boolean afterSuggestBox) {
            Button openTooltip = new Button("..");
            openTooltip.setStyleName("tooltipButton");
            openTooltip.addClickHandler(new TooltipButtonClickHandler(localEventBus));
            int index = afterSuggestBox ? container.getChildCount() - 1 : container.getChildCount();
            super.insert(openTooltip, container, index, true);

        }

        private void changeSuggestInputWidth() {

            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    String suggestWidth = null;
                    Widget lastWidget = getLastWidget();
                    if (lastWidget == null || lastWidget.getElement() == null) {
                        suggestWidth = "100%";
                    } else {
                        int width = container.getAbsoluteRight() - lastWidget.getElement().getAbsoluteRight() - INPUT_MARGIN;
                        suggestWidth = width > 0 ? width + "px" : "100%";
                    }
                    suggestBox.setWidth(suggestWidth);

                }
            });

        }

        public void clearAllItems() {
            for (Iterator<Widget> it = getChildren().iterator(); it.hasNext(); ) {
                final Widget widget = it.next();
                if (widget instanceof SelectedItemComposite) {
                    it.remove();
                }
            }
        }

        private Widget getLastWidget() {
            Widget tooltipButtonElement = getTooltipButton();
            if (tooltipButtonElement != null) {
                return tooltipButtonElement;
            }
            SelectedItemComposite lastItem = getLastItem();
            return lastItem;
        }

        private void clearAll(){
            clearAllItems();
            Widget widget = getTooltipButton();
            if(widget != null){
                widget.removeFromParent();
            }
        }

        private Widget getTooltipButton() {

            WidgetCollection collection = getChildren();
            int lastElementIndex = collection.size() - 1;
            for (int i = lastElementIndex; i >= 0; i--) {
                Widget widget = collection.get(i);
                if (widget instanceof Button) {
                    return widget;
                }
            }
            return null;
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
            if (currentState.isSingleChoice()) {
                currentState.getSelectedIds().clear();
                clearAllItems();
                currentState.getListValues().clear();
            }
            selectedSuggestions.put(itemId, itemName);
            int index = shouldDrawTooltipButton(currentState) ? container.getChildCount() - 2
                    : container.getChildCount() - 1;
            super.insert(itemComposite, container, index, true);

        }

        private EventListener createCloseBtnListener(final SelectedItemComposite itemComposite) {
            return new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    remove(itemComposite);
                    Id id = itemComposite.getItemId();
                    tryToPoolFromTooltipContent();
                    removeSuggestItemFromStates(id);
                    changeSuggestInputWidth();

                }
            };
        }

        private EventListener createHyperlinkListener(final SelectedItemComposite itemComposite) {
            return new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {

                    HyperlinkClickHandler clickHandler = new HyperlinkClickHandler(itemComposite.getItemId(), null,
                            localEventBus, false, currentState.getTypeTitleMap(), SuggestBoxWidget.this);
                    clickHandler.processClick();
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
                if(suggestBox.getText().equalsIgnoreCase(ALL_SUGGESTIONS)){
                suggestBox.setText(EMPTY_VALUE);
                }

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
                SuggestPresenter presenter = (SuggestPresenter) impl;
                presenter.changeSuggestionsPopupSize();
                SuggestOracle.Response response = new SuggestOracle.Response();
                response.setSuggestions(suggestions);
                callback.onSuggestionsReady(request, response);
                ((SuggestBoxDisplay) suggestBox.getSuggestionDisplay())
                        .setScrollPosition(lastScrollPos);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining suggestions for '" + request.getQuery() + "'", caught);
            }
        });
        GWT.log("suggestion requested " + request.getQuery());
    }

}
