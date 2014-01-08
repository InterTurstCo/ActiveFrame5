package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserNodeView implements IsWidget {
    private List<HierarchyBrowserItem> items = new ArrayList<HierarchyBrowserItem>();
    private VerticalPanel currentNodePanel = new VerticalPanel();
    private VerticalPanel root = new VerticalPanel();
    private EventBus eventBus;
    private int factor;
    private ScrollPanel scroll = new ScrollPanel();
    private TextBox textBox;
    private boolean dontShowResetButton;
    private Id parentId;

    public HierarchyBrowserNodeView(EventBus eventBus, int nodeHeight, Id parentId) {
        this.eventBus = eventBus;
        this.parentId = parentId;
        scroll.setHeight(nodeHeight + "px");
        scroll.setAlwaysShowScrollBars(false);
        scroll.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        root.addStyleName("node");
        scroll.addStyleName("one-node-scroll");
        currentNodePanel.addStyleName("one-node-body");
    }

    public void setItems(List<HierarchyBrowserItem> items) {

        this.items = items;
    }

    public void drawMoreItems(List<HierarchyBrowserItem> moreItems) {
        items.addAll(moreItems);
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
        scroll.setHorizontalScrollPosition(0);
    }

    public void redrawNode(List<HierarchyBrowserItem> items, Id parentId) {
        this.items = items;
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
        scroll.setHorizontalScrollPosition(0);
    }


    @Override
    public Widget asWidget() {
        return root;
    }

    public void drawNode() {
        currentNodePanel.clear();
        final HierarchyBrowserItem currentItem = items.get(0);
        drawNodeHeader(currentItem);
        scroll.add(currentNodePanel);
        root.add(scroll);
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
        scroll.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {

                if (scroll.getVerticalScrollPosition() == scroll.getMaximumVerticalScrollPosition()) {
                    factor++;
                    eventBus.fireEvent(new HierarchyBrowserScrollEvent(currentItem.getNodeCollectionName(), parentId,
                            factor, textBox.getText()));
                }
            }
        });
    }

    public void drawNodeChild(final HierarchyBrowserItem item) {
        HorizontalPanel currentItemPanel = new HorizontalPanel();
        currentItemPanel.setWidth("100%");
        final CheckBox checkBox = new CheckBox();
        if (item.isChosen()) {
            checkBox.setValue(true);
        }
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean chosen = event.getValue();
                item.setChosen(chosen);
                eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(item));
            }
        });

        Anchor anchor = new Anchor(item.getStringRepresentation());
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserItemClickEvent(item));
            }
        });

        FocusPanel focusPanel = new FocusPanel();

        Label arrow = new Label("►");
        arrow.getElement().getStyle().setFloat(Style.Float.RIGHT);
        focusPanel.add(arrow);
        focusPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserNodeClickEvent(item.getNodeCollectionName(), item.getId()));

            }
        });
        HorizontalPanel panelLeft = new HorizontalPanel();
        panelLeft.add(checkBox);
        panelLeft.add(anchor);
        currentItemPanel.add(panelLeft);
        currentItemPanel.add(focusPanel);
        currentNodePanel.add(currentItemPanel);

    }

    private void drawNodeHeader(HierarchyBrowserItem item) {

        HorizontalPanel searchBar = createSearchBar(item);
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
        FocusPanel refreshButton = createRefreshButton(item);
        FocusPanel addItemPanel = createAddItemButton(item);
        buttonsPanel.add(addItemPanel);
        buttonsPanel.add(refreshButton);
        root.add(searchBar);
        root.add(buttonsPanel);
    }

    private FocusPanel createAddItemButton(final HierarchyBrowserItem item) {
        FocusPanel addItemPanel = new FocusPanel();
        addItemPanel.setStyleName("composite-button");
        HorizontalPanel buttonPanel = new HorizontalPanel();
        Image plus = new Image("images/green-plus.png");
        Label text = new Label(item.getNodeCollectionName());
        buttonPanel.add(plus);
        buttonPanel.add(text);
        addItemPanel.add(buttonPanel);
        addItemPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserItemClickEvent(item));
            }
        });
        return addItemPanel;
    }

    private FocusPanel createRefreshButton(final HierarchyBrowserItem item) {
        FocusPanel refreshButton = new FocusPanel();
        refreshButton.setStyleName("button-refresh");
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(item.getNodeCollectionName(), parentId));
                factor = 0;
            }
        });

        return refreshButton;
    }

    private HorizontalPanel createSearchBar(final HierarchyBrowserItem item) {
        HorizontalPanel searchBar = new HorizontalPanel();
        searchBar.addStyleName("search-bar");
        textBox = new TextBox();
        textBox.removeStyleName("gwt-TextBox");
        textBox.getElement().setAttribute("placeholder", "Поиск");
        textBox.addStyleName("input-text");
        Image magnifier = new Image("images/loupe.png");
        FocusPanel magnifierPanel = new FocusPanel();
        magnifierPanel.addStyleName("magnifier");
        magnifierPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String inputText = textBox.getText();
                if ("".equalsIgnoreCase(inputText)) {
                    return;
                }
                eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(item.getNodeCollectionName(), parentId, inputText));

            }
        });
        magnifierPanel.add(magnifier);
        searchBar.add(magnifierPanel);
        searchBar.add(textBox);

        final FocusPanel clearInputPanel = new FocusPanel();
        searchBar.add(clearInputPanel);
        textBox.addValueChangeHandler(new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                if (dontShowResetButton) {
                    return;
                }
                String inputText = (String) event.getValue();
                if ("".equalsIgnoreCase(inputText)) {
                    clearInputPanel.clear();
                    dontShowResetButton = false;
                }
                final Image resetButton = new Image("images/ico-delete.gif");
                resetButton.addStyleName("reset-button");
                resetButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        textBox.setText("");
                        eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(item.getNodeCollectionName(), parentId));
                        clearInputPanel.clear();
                        dontShowResetButton = false;
                        factor = 0;
                    }
                });
                clearInputPanel.add(resetButton);
                dontShowResetButton = true;
            }

        });
        return searchBar;
    }

}
