package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;

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

    private boolean selective;
    private HorizontalPanel styledActivePanel = new HorizontalPanel();

    public HierarchyBrowserNodeView(EventBus eventBus, int nodeHeight,
                                    boolean selective) {
        this.eventBus = eventBus;
        this.selective = selective;
        scroll.setHeight(nodeHeight + "px");
        scroll.setAlwaysShowScrollBars(false);
        scroll.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        root.addStyleName("node");
        scroll.addStyleName("one-node-scroll");
        currentNodePanel.addStyleName("one-node-body");
    }

    public void drawMoreItems(List<HierarchyBrowserItem> moreItems, NodeMetadata metadata) {
        items.addAll(moreItems);
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item, metadata);
        }

    }

    public void redrawNode(List<HierarchyBrowserItem> items, NodeMetadata nodeMetadata) {
        scroll.setHorizontalScrollPosition(0);
        this.items = items;
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item, nodeMetadata);
        }
        factor = 0;

    }


    @Override
    public Widget asWidget() {
        return root;
    }

    public void drawNode(List<HierarchyBrowserItem> items, final NodeMetadata nodeMetadata) {
        this.items = items;
        currentNodePanel.clear();
        drawNodeHeader(nodeMetadata);
        scroll.add(currentNodePanel);
        root.add(scroll);
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item, nodeMetadata);
        }
        scroll.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {

                if (scroll.getVerticalScrollPosition() == scroll.getMaximumVerticalScrollPosition()) {
                    factor++;
                    eventBus.fireEvent(new HierarchyBrowserScrollEvent(nodeMetadata,
                            factor, textBox.getText()));
                }
            }
        });
    }

    public void drawNodeChild(final HierarchyBrowserItem item, final NodeMetadata nodeMetadata) {
        final HorizontalPanel currentItemPanel = new HorizontalPanel();
        HorizontalPanel panelLeft = new HorizontalPanel();
        if (selective) {
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
            eventBus.addHandler(HierarchyBrowserCheckBoxUpdateEvent.TYPE,new HierarchyBrowserCheckBoxUpdateEventHandler() {
                @Override
                public void onHierarchyBrowserCheckBoxUpdate(HierarchyBrowserCheckBoxUpdateEvent event) {

                    if(item.getId().equals(event.getItem().getId())){
                     boolean value = event.getItem().isChosen();
                    checkBox.setValue(value);
                    }
                }
            });
            panelLeft.add(checkBox);
        }

        Label anchor = new Label(item.getStringRepresentation());
        anchor.addStyleName("node-item-link");
        anchor.removeStyleName("gwt-Hyperlink ");
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserItemClickEvent(nodeMetadata, item.getId()));

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
                styledActivePanel.removeStyleName("node-item-row-active");
                currentItemPanel.addStyleName("node-item-row-active");
                styledActivePanel = currentItemPanel;
            }
        });
        panelLeft.add(anchor);
        currentItemPanel.add(panelLeft);
        currentItemPanel.add(focusPanel);
        currentItemPanel.addStyleName("node-item-row");
        final String id = item.getId().toStringRepresentation();
        currentItemPanel.getElement().setId(id);
        currentItemPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (childClicked(event, id)) {
                    return;
                }
                eventBus.fireEvent(new HierarchyBrowserNodeClickEvent(item.getNodeCollectionName(), item.getId()));
                styledActivePanel.removeStyleName("node-item-row-active");
                currentItemPanel.addStyleName("node-item-row-active");
                styledActivePanel = currentItemPanel;
            }
        }, ClickEvent.getType());
        currentNodePanel.add(currentItemPanel);
    }

    private void drawNodeHeader(NodeMetadata nodeMetadata) {

        AbsolutePanel searchBar = createSearchBar(nodeMetadata);
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
        FocusPanel refreshButton = createRefreshButton(nodeMetadata);
        FocusPanel addItemPanel = createAddItemButton(nodeMetadata);
        buttonsPanel.add(addItemPanel);
        buttonsPanel.add(refreshButton);
        root.add(searchBar);
        root.add(buttonsPanel);
    }

    private FocusPanel createAddItemButton(final NodeMetadata nodeMetadata) {
        final FocusPanel addItemPanel = new FocusPanel();
        addItemPanel.setStyleName("composite-button");
        HorizontalPanel buttonPanel = new HorizontalPanel();
        Image plus = new Image("images/green-plus.png");
        Label text = new Label(nodeMetadata.getDomainObjectType());
        buttonPanel.add(plus);
        buttonPanel.add(text);
        addItemPanel.add(buttonPanel);
        addItemPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserAddItemClickEvent(nodeMetadata));

            }
        });
        return addItemPanel;
    }

    private FocusPanel createRefreshButton(final NodeMetadata nodeMetadata) {
        FocusPanel refreshButton = new FocusPanel();
        refreshButton.setStyleName("button-refresh");
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(nodeMetadata));
                factor = 0;
            }
        });

        return refreshButton;
    }

    private AbsolutePanel createSearchBar(final NodeMetadata nodeMetadata) {
        final AbsolutePanel searchBar = new AbsolutePanel();
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
                eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(nodeMetadata, inputText));

            }
        });
        magnifierPanel.add(magnifier);
        searchBar.add(magnifierPanel);
        searchBar.add(textBox);
        final Image resetButton = new Image("images/icon-delete.png");
        textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                if (dontShowResetButton) {
                    return;
                }
                String inputText = textBox.getValue();
                if ("".equalsIgnoreCase(inputText)) {
                    resetButton.removeFromParent();
                    dontShowResetButton = false;
                }

                resetButton.addStyleName("reset-button");
                resetButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        textBox.setText("");
                        eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(nodeMetadata));
                        resetButton.removeFromParent();
                        dontShowResetButton = false;
                        factor = 0;
                    }
                });
                searchBar.add(resetButton);
                dontShowResetButton = true;
            }

        });
        textBox.addKeyDownHandler(new KeyDownHandler() {

            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    String inputText = textBox.getValue();
                    eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(nodeMetadata, inputText));
                }
            }
        });
        return searchBar;
    }

    private boolean childClicked(ClickEvent event, String id) {
        NodeList<Element> checkBoxes = Document.get().getElementById(id).getElementsByTagName("input");
        Element target = Element.as(event.getNativeEvent().getEventTarget());
        for (int i = 0; i < checkBoxes.getLength(); i++) {
            if (checkBoxes.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        NodeList<Element> links = Document.get().getElementById(id).getElementsByTagName("div");
        for (int i = 0; i < links.getLength(); i++) {
            if (links.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        return false;
    }
}
