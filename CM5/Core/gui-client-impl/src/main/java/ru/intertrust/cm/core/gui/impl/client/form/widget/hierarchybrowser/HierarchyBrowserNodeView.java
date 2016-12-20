package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.api.client.PanelResizeListener;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.HierarchyConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.SEARCH_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.EMPTY_VALUE;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.SEARCH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserNodeView implements IsWidget {
    public static final Double NODE_VIEW_HEIGHT_FACTOR = 0.60;
    private static final int HEADER_HEIGHT = 50;
    private static final int HEIGHT_OFFSET = 20;
    private List<HierarchyBrowserItem> items = new ArrayList<HierarchyBrowserItem>();
    private VerticalPanel currentNodePanel = new VerticalPanel();
    private VerticalPanel root = new VerticalPanel();
    private EventBus eventBus;
    private int factor;
    private ScrollPanel scroll = new ScrollPanel();
    private TextBox textBox;
    private List<HierarchyCheckBoxesWrapper> checkBoxesWrappers;
    private int recursionDeepness;
    private HorizontalPanel styledActivePanel = new HorizontalPanel();
    private boolean displayAsHyperlinks;

    public HierarchyBrowserNodeView(EventBus eventBus, int nodeHeight, boolean displayAsHyperlinks) {

        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        scroll.setHeight(nodeHeight - HEADER_HEIGHT - HEIGHT_OFFSET + "px");
        scroll.setAlwaysShowScrollBars(false);
        scroll.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        root.addStyleName("hierarchyBrowserNode");
        scroll.addStyleName("oneNodeScroll");
        currentNodePanel.addStyleName("one-node-body");
        checkBoxesWrappers = new ArrayList<HierarchyCheckBoxesWrapper>();
        HierarchyCheckBoxesManager checkBoxesManager = new HierarchyCheckBoxesManager(checkBoxesWrappers, eventBus);
        checkBoxesManager.activate();

    }

    public void drawMoreItems(List<HierarchyBrowserItem> moreItems) {
        items.addAll(moreItems);
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }

    }

    public void redrawNode(List<HierarchyBrowserItem> items) {
        checkBoxesWrappers.clear();
        scroll.setHorizontalScrollPosition(0);
        scroll.setVerticalScrollPosition(0);
        this.items = items;
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
        factor = 0;

    }

    @Override
    public Widget asWidget() {
        return root;
    }

    public void drawNode(Id parentId, String parentCollectionName, final List<HierarchyBrowserItem> items,
                         List<NodeCollectionDefConfig> nodeConfigs) {
        this.items = items;
        currentNodePanel.clear();
        drawNodeHeader(parentId, parentCollectionName, nodeConfigs);
        scroll.add(currentNodePanel);
        root.add(scroll);
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
        addScrollHandler(parentId, parentCollectionName);
    }

    private void addScrollHandler(final Id parentId, final String parentCollectionName) {
        scroll.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {

                if (scroll.getVerticalScrollPosition() == scroll.getMaximumVerticalScrollPosition() ||
                        scroll.getMaximumVerticalScrollPosition()-scroll.getVerticalScrollPosition()==1) {
                    factor++;
                    eventBus.fireEvent(new HierarchyBrowserScrollEvent(parentId, parentCollectionName,
                            factor, textBox.getText(), recursionDeepness));
                }
            }
        });
    }

    public void drawNodeChild(final HierarchyBrowserItem item) {
        final HorizontalPanel currentItemPanel = new HorizontalPanel();
        HorizontalPanel panelLeft = new HorizontalPanel();
        if (item.isSelective()) {
            CheckBox checkBox = createCheckBox(item);
            panelLeft.add(checkBox);
            panelLeft.setCellVerticalAlignment(checkBox, HasVerticalAlignment.ALIGN_MIDDLE);
        }

        Label label = createLabel(item);
        FocusPanel focusPanel = new FocusPanel();
        if (item.isMayHaveChildren()) {
            Label arrow = new Label("►");
            arrow.addStyleName("hBArrowRight");
            focusPanel.add(arrow);
            currentItemPanel.addStyleName("node-item-row");
            focusPanel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    eventBus.fireEvent(new HierarchyBrowserNodeClickEvent(item.getNodeCollectionName(), item.getId(), recursionDeepness));
                    styledActivePanel.removeStyleName("node-item-row-active");
                    currentItemPanel.addStyleName("node-item-row-active");
                    styledActivePanel = currentItemPanel;
                }
            });

        }
        panelLeft.add(label);
        currentItemPanel.add(panelLeft);
        currentItemPanel.add(focusPanel);

        final String id = item.getId().toStringRepresentation();
        currentItemPanel.getElement().setId(recursionDeepness + id);
        currentItemPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GuiUtil.isChildClicked(event, recursionDeepness + id) || !item.isMayHaveChildren()) {
                    return;
                }
                eventBus.fireEvent(new HierarchyBrowserNodeClickEvent(item.getNodeCollectionName(), item.getId(), recursionDeepness));
                styledActivePanel.removeStyleName("node-item-row-active");
                currentItemPanel.addStyleName("node-item-row-active");
                styledActivePanel = currentItemPanel;
            }
        }, ClickEvent.getType());
        currentNodePanel.add(currentItemPanel);
    }

    private Label createLabel(final HierarchyBrowserItem item) {
        Label label = new Label(item.getStringRepresentation());
        boolean isHyperlink = item.isDisplayAsHyperlinks() == null ? displayAsHyperlinks : item.isDisplayAsHyperlinks();
        if (isHyperlink) {
            label.setStyleName("clickableHierarchyLabel");
            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    eventBus.fireEvent(new HierarchyBrowserItemClickEvent(item, null, false));

                }
            });
        } else {
            label.setStyleName("notClickableHierarchyLabel");
        }
        return label;
    }

    private CheckBox createCheckBox(final HierarchyBrowserItem item) {
        CheckBox checkBox = new CheckBox();
        if (item.isChosen()) {
            checkBox.setValue(true);
        }
        HierarchyCheckBoxValueChangeHandler handler = new HierarchyCheckBoxValueChangeHandler(item, eventBus);
        checkBox.addValueChangeHandler(handler);
        checkBoxesWrappers.add(new HierarchyCheckBoxesWrapper(checkBox, item));

        return checkBox;
    }

    private void drawNodeHeader(Id parentId, String parentCollectionName, List<NodeCollectionDefConfig> nodeConfigs) {

        AbsolutePanel searchBar = createSearchBar(parentId, parentCollectionName);
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);

        for (NodeCollectionDefConfig config : nodeConfigs) {
            if (config.isDisplayingCreateButton()) {
                Widget addItem = createAddItemButton(parentId, parentCollectionName, config, buttonsPanel);
                buttonsPanel.add(addItem);
            }
            if (config.getCollection().equalsIgnoreCase(parentCollectionName)) {
                recursionDeepness = config.getRecursiveDeepness() > 0 ? config.getRecursiveDeepness() : 0;
            }
        }
        FocusPanel refreshButton = createRefreshButton(parentId, parentCollectionName);
        buttonsPanel.add(refreshButton);
        root.add(searchBar);
        root.add(buttonsPanel);
    }

    private Widget createAddItemButton(final Id parentId, final String parentCollectionName,
                                       final NodeCollectionDefConfig nodeConfig, UIObject uiObject) {
        ConfiguredButton button = new HierarchyConfiguredButton(nodeConfig.getCreateNewButtonConfig());
        button.addClickHandler(new HierarchyBrowserAddClickHandler(parentId, parentCollectionName, nodeConfig,
                eventBus, uiObject, recursionDeepness));
        return button;
    }

    private FocusPanel createRefreshButton(final Id parentId, final String parentCollectionName) {
        FocusPanel refreshButton = new FocusPanel();
        refreshButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().refreshBtn());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName,
                        textBox.getText(), recursionDeepness));
                factor = 0;
            }
        });

        return refreshButton;
    }

    private AbsolutePanel createSearchBar(final Id parentId, final String parentCollectionName) {
        final AbsolutePanel result = new AbsolutePanel();
        result.addStyleName("search-bar");
        textBox = new TextBox();
        Panel resetButton = initResetButton(parentId, parentCollectionName);
        initFilterInput(resetButton, parentId, parentCollectionName);
        Panel magnifier = initMagnifier(parentId, parentCollectionName);
        result.add(magnifier);
        result.add(textBox);
        result.add(resetButton);

        return result;
    }

    private Panel initMagnifier(final Id parentId, final String parentCollectionName) {
        Panel magnifier = new AbsolutePanel();
        magnifier.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().magnifierButton());
        FocusPanel result = new FocusPanel();
        result.addStyleName("magnifier");
        result.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String inputText = textBox.getText();
                if ("".equalsIgnoreCase(inputText)) {
                    return;
                }
                eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(parentId, parentCollectionName, inputText, recursionDeepness));

            }
        });
        result.add(magnifier);
        return result;
    }

    private Panel initResetButton(final Id parentId, final String parentCollectionName) {
        final Panel result = new AbsolutePanel();
        result.addStyleName("reset-button");
        result.getElement().getStyle().setDisplay(Style.Display.NONE);
        result.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().hBFilterClearButton());
        result.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                textBox.setText(EMPTY_VALUE);
                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName,
                        textBox.getText(), recursionDeepness));
                result.removeFromParent();
                factor = 0;
            }
        }, ClickEvent.getType());

        return result;
    }

    private void initFilterInput(final Panel resetButton, final Id parentId, final String parentCollectionName) {
        textBox.getElement().setAttribute("placeholder", LocalizeUtil.get(SEARCH_KEY, SEARCH));
        textBox.setStyleName("input-text");
        textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                Style style = resetButton.getElement().getStyle();
                String inputText = textBox.getText();
                if (EMPTY_VALUE.equalsIgnoreCase(inputText)) {
                    style.setDisplay(Style.Display.NONE);
                } else {
                    style.clearDisplay();

                }
            }

        });
        textBox.addKeyDownHandler(new KeyDownHandler() {

            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    String inputText = textBox.getValue();
                    eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(parentId, parentCollectionName, inputText, recursionDeepness));
                }
            }
        });

    }

    public void refreshNode(HierarchyBrowserItem item) {
        if (HierarchyBrowserUtil.handleUpdateChosenItem(item, items)) {
            redrawNode(items);
        }
    }

    public PanelResizeListener getPanelResizeListener() {
        return new NodeViewResizeListener();
    }

    private class NodeViewResizeListener implements PanelResizeListener {

        @Override
        public void onPanelResize(int width, int height) {
            int newNodeHeight = (int) (NODE_VIEW_HEIGHT_FACTOR * height) - HEIGHT_OFFSET;
            root.setHeight(newNodeHeight + "px");
            scroll.setHeight(newNodeHeight - HEADER_HEIGHT + "px");

        }
    }

}
