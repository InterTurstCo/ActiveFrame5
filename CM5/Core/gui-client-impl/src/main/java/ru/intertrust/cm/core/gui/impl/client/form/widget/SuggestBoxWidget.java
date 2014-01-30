package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.MultiWordIdentifiableSuggestion;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

@ComponentName("suggest-box")
public class SuggestBoxWidget extends BaseWidget {

    private SuggestBox suggestBox;
   // private
    private final HashMap<Id, String> allSuggestions = new HashMap<Id, String>();
    CmjDefaultSuggestionDisplay display;

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
            timer.scheduleRepeating(100);
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
        label.removeStyleName("gwt-Label");
        return label;
    }

    public class CmjDefaultSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
        public PopupPanel getSuggestionPopup() {
            return this.getPopupPanel();
        }

    }

    @Override
    protected Widget asEditableWidget() {
        final SuggestPresenter presenter = new SuggestPresenter();
        //presenter.setSuggestMaxDropDownWidth(suggestMaxDropDownWidth);

        MultiWordSuggestOracle oracle = buildDynamicMultiWordOracle();
        //suggestBox = new SuggestBox(oracle);
        suggestBox = new SuggestBox(oracle, new TextBox(), new CmjDefaultSuggestionDisplay());
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

//        SuggestBox.DefaultSuggestionDisplay display = (SuggestBox.DefaultSuggestionDisplay) suggestBox.getSuggestionDisplay();
//        display.setPositionRelativeTo(presenter);


        display = (CmjDefaultSuggestionDisplay) suggestBox.getSuggestionDisplay();

        display.setPositionRelativeTo(presenter);
//+------------------------------------------------------
//|.gwt-SuggestBoxPopup
//+------------------------------------------------------
//        display.getSuggestionPopup().getElement().getStyle().setWidth(700, Style.Unit.PX);
//        display.getSuggestionPopup().getElement().getStyle().setHeight(100, Style.Unit.PX);


        display.getSuggestionPopup().getElement().getStyle().setZIndex(999999999);

        return presenter;
    }

    private void initState(final SuggestBoxState state, final SuggestBox suggestBox) {
        if (isEditable()) {
            final SuggestPresenter presenter = (SuggestPresenter) impl;
            presenter.init(state, suggestBox);
        } else {
//            final int maxWidth = impl.getElement().getParentElement().getClientWidth() - 4;
//            impl.getElement().getStyle().setProperty("maxWidth", maxWidth, Style.Unit.PX);
            final StringBuilder builder = new StringBuilder();
            final HashMap<Id, String> listValues = state.getListValues();
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
                suggestionRequest.setSortCriteriaConfig(suggestBoxConfig.getSortCriteriaConfig());
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
        private boolean singleChoice;
        private Element container;
        private Element arrowBtn;
        private Element clearAllButton;
        private SuggestBox suggestBox;
        private Integer maxDropDownWidth;
        private Integer maxDropDownHeight;
        private int preferableWidth;

          private SuggestPresenter() {
            Element row = DOM.createTR();
            this.selectedSuggestions = new HashMap<Id, String>();
            setStyleName("suggest-container");
            container = DOM.createTD();
            DOM.appendChild(row, container);
            DOM.appendChild(getBody(), row);
            arrowBtn = DOM.createTD();
            arrowBtn.setClassName("arrow-suggest-btn");
     //       DOM.appendChild(row, arrowBtn);
            DOM.setEventListener(arrowBtn, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    final CmjDefaultSuggestionDisplay display = (CmjDefaultSuggestionDisplay) suggestBox.getSuggestionDisplay();


                    Element e = (Element) display.getSuggestionPopup().getElement().getFirstChild().getFirstChild().getFirstChild().getChild(1).getChild(1).getFirstChild();


                    suggestBox.setText("*");
                    //suggestBox.showSuggestionList();
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
                    }
                    else {
//                        //если вверху
//                        suggestBox.showSuggestionList();
//                        if(suggestBox.getAbsoluteTop() < ((CmjDefaultSuggestionDisplay) suggestBox.getSuggestionDisplay()).getSuggestionPopup().getAbsoluteTop()){
//
//                            e.getStyle().setHeight(suggestBox.getAbsoluteTop() - suggestBox.getOffsetHeight(), Style.Unit.PX);
//                            e.getStyle().setOverflowY(Style.Overflow.SCROLL);
//                        }
//                        //если внизу
                        suggestBox.showSuggestionList();
                        e.getStyle().setHeight(Window.getClientHeight()- suggestBox.getAbsoluteTop() - suggestBox.getOffsetHeight() - 25, Style.Unit.PX);
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
            clearAllButton.getStyle().setMarginRight(-69, Style.Unit.PX);
            clearAllButton.getStyle().setDisplay(Style.Display.BLOCK);
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

        private void setMaxDropDownWidth(Integer maxDropDownWidth) {
            this.maxDropDownWidth = maxDropDownWidth;
        }

        private Integer getMaxDropDownHeight() {
            return maxDropDownHeight;
        }

        private void setMaxDropDownHeight(Integer maxDropDownHeight) {
            this.maxDropDownHeight = maxDropDownHeight;
        }

        private int getNumberFromSizeString(String sizeString) {
            if (sizeString == null || sizeString.equalsIgnoreCase("")) {
                return 0;
            }
            int UnitPx = 2;
            return Integer.parseInt(sizeString.substring(0, sizeString.length() - UnitPx));
        }

        public void init(final SuggestBoxState state, final SuggestBox suggestBox) {
            if (state.getSuggestBoxConfig().getMaxDropDownWidth() != null) {
                this.maxDropDownWidth = state.getSuggestBoxConfig().getMaxDropDownWidth();
            }
            if (state.getSuggestBoxConfig().getMaxDropDownHeight() != null) {
                this.maxDropDownHeight = state.getSuggestBoxConfig().getMaxDropDownHeight();
            }

            this.singleChoice = state.isSingleChoice();
            clear();
            selectedSuggestions.clear();
            this.suggestBox = suggestBox;
            if (getElement().getStyle().getProperty("maxWidth").isEmpty()) {
                preferableWidth = getElement().getClientWidth();
                if (state.getSuggestBoxConfig().getClearAllButtonConfig() != null){
                    int clearButtonWidth = 48; // using hardcode as clearAllButton.getClientWidth() returns wrong value (because of rightMargin = -69)
                    preferableWidth = preferableWidth - clearButtonWidth;
                }
                container.getStyle().setWidth(100, Style.Unit.PCT);
            }
            final HashMap<Id, String> listValues = state.getListValues();
            for (final Map.Entry<Id, String> listEntry : listValues.entrySet()) {
                final SelectedItemComposite itemComposite =
                        new SelectedItemComposite(listEntry.getKey(), listEntry.getValue());
                itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
                super.add(itemComposite, container);
                selectedSuggestions.put(listEntry.getKey(), listEntry.getValue());
            }
            super.add(suggestBox, container);
            if (state.getSuggestBoxConfig().getClearAllButtonConfig() != null){
            FocusPanel focusPanel = new FocusPanel();
            ButtonForm clearButton = new ButtonForm(focusPanel,
                    state.getSuggestBoxConfig().getClearAllButtonConfig().getImage(),
                    state.getSuggestBoxConfig().getClearAllButtonConfig().getText());
            focusPanel.add(clearButton);
            focusPanel.getElement().getStyle().setLeft(10, Style.Unit.PX);
            super.add(focusPanel, clearAllButton);
            focusPanel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    for (int i = selectedSuggestions.size() - 1; i >= 0; i--) {
                        remove(i);
                    }
                    selectedSuggestions.clear();
                    updateSuggestBoxWidth();
                }
            });
            }

            updateSuggestBoxWidth();
        }

        public void insert(final Id itemId, final String itemName) {
            final SelectedItemComposite itemComposite = new SelectedItemComposite(itemId, itemName);
            itemComposite.setCloseBtnListener(createCloseBtnListener(itemComposite));
            if (singleChoice) {
                selectedSuggestions.clear();
                for (Iterator<Widget> it = getChildren().iterator(); it.hasNext(); ) {
                    final Widget widget = it.next();
                    if (widget instanceof SelectedItemComposite) {
                        it.remove();
                    }
                }
            }
            selectedSuggestions.put(itemId, itemName);
            final int index = container.getChildCount() - 1;
            super.insert(itemComposite, container, index, true);
            updateSuggestBoxWidth();
        }

        private void updateSuggestBoxWidth() {
            getElement().getStyle().setProperty("width", preferableWidth, Style.Unit.PX);
            int  maxChildWidth = 0;
            for (Widget child : this) {
                if (child instanceof SelectedItemComposite) {
                    if (child.getOffsetWidth() > maxChildWidth ) {
                        maxChildWidth = child.getOffsetWidth();
                    }
                }
            }
            maxChildWidth = maxChildWidth + arrowBtn.getOffsetWidth() + 25;
            if (maxChildWidth > preferableWidth) {
                getElement().getStyle().setProperty("width", maxChildWidth, Style.Unit.PX);
            }
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
