package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CANCELLATION_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.DONE_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.HierarchyBrowserNodeView.NODE_VIEW_HEIGHT_FACTOR;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCELLATION_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DONE_BUTTON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.api.client.PanelResizeListener;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HyperLinkWithHistorySupport;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.CaptionCloseButton;
import ru.intertrust.cm.core.gui.impl.client.panel.ResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.panel.RightSideResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Yaroslav Bondarchuk Date: 26.12.13 Time: 11:15
 */
public class HierarchyBrowserMainPopup implements HierarchyBrowserDisplay {

    public static final int DEFAULT_WIDTH = 1000;
    public static final int DEFAULT_HEIGHT = 400;
    public static final int MINIMAL_WIDTH = 500;
    public static final int MINIMAL_HEIGHT = 300;
    private static final int MAIN_CONTENT_MARGIN = 5;

    private static final Double LINKS_SECTION_WIDTH_FACTOR = 0.1;
    private static final Double POPUP_CHOSEN_CONTENT_HEIGHT_FACTOR = 0.10;
    private static final Double NODE_SECTION_WIDTH_FACTOR = 0.9;

    private double nodeSectionWidth;
    private double nodeSectionHeight;

    private HierarchyBrowserItemsView popupChosenContent;
    private HorizontalPanel nodesSection;
    private DialogBox dialogBox;
    private final int popupWidth;
    private final int popupHeight;
    private HyperLinkWithHistorySupport linkLabel;

    private final EventBus eventBus;
    private Button okButton;
    private final Map<String, HierarchyBrowserNodeView> containerMap;
    private final List<String> nodeTypes;
    private final SelectionStyleConfig selectionStyleConfig;
    private final boolean displayAsHyperlinks;
    private Button cancelButton;

    private final String title;
    private final boolean shouldDrawTooltipButton;
    private CaptionCloseButton captionCloseButton;
    private ResizablePanel resizablePanel;

    private final boolean resizable;

    public HierarchyBrowserMainPopup(EventBus eventBus, HierarchyBrowserWidgetState state) {
        this.eventBus = eventBus;

        this.selectionStyleConfig = state.getHierarchyBrowserConfig().getSelectionStyleConfig();
        this.displayAsHyperlinks = HierarchyBrowserUtil.isDisplayingHyperlinks(state);
        this.shouldDrawTooltipButton = state.isTooltipAvailable();
        title = state.getRootNodeLinkConfig() == null ? "link" : state.getRootNodeLinkConfig().getTitle();
        containerMap = new HashMap<String, HierarchyBrowserNodeView>();
        nodeTypes = new ArrayList<String>();
        DialogWindowConfig dialogWindowConfig = state.getHierarchyBrowserConfig().getDialogWindowConfig();
        popupWidth = HierarchyBrowserUtil.getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getWidth() : null, HierarchyBrowserMainPopup.DEFAULT_WIDTH);
        popupHeight = HierarchyBrowserUtil.getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getHeight() : null, HierarchyBrowserMainPopup.DEFAULT_HEIGHT);
        this.resizable = GuiUtil.isDialogWindowResizable(dialogWindowConfig);

    }

    public void hidePopup() {
        dialogBox.hide();
    }

    private Widget initPopup() {
        resizablePanel = new RightSideResizablePanel(MINIMAL_WIDTH, MINIMAL_HEIGHT, true, resizable);
        final VerticalPanel root = new VerticalPanel();

        HorizontalPanel linksAndNodesSection = new HorizontalPanel();
        linksAndNodesSection.addStyleName("grey-background");
        VerticalPanel linksSection = new VerticalPanel();
        linksSection.addStyleName("hierarchyLinksSection");
        resizablePanel.addResizeListener(new HierarchyBrowserElementResizeListener(linksAndNodesSection, null, NODE_VIEW_HEIGHT_FACTOR));
        // linksSection.setWidth(LINKS_SECTION_WIDTH_FACTOR * popupWidth +
        // "px");
        // resizablePanel.addResizeListener(new
        // HierarchyBrowserElementResizeListener(linksSection,
        // LINKS_SECTION_WIDTH_FACTOR, null));
        popupChosenContent = new HierarchyBrowserItemsView(selectionStyleConfig, eventBus, displayAsHyperlinks);
        popupChosenContent.setHeight(POPUP_CHOSEN_CONTENT_HEIGHT_FACTOR * popupHeight + "px");
        resizablePanel.addResizeListener(new HierarchyBrowserElementResizeListener(popupChosenContent, null, POPUP_CHOSEN_CONTENT_HEIGHT_FACTOR));
        popupChosenContent.asWidget().addStyleName("popup-chosen-content");

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.addStyleName("node-section-scroll");
        nodesSection = new HorizontalPanel();
        nodeSectionWidth = NODE_SECTION_WIDTH_FACTOR * popupWidth;
        nodeSectionHeight = NODE_VIEW_HEIGHT_FACTOR * popupHeight;
        resizablePanel.addResizeListener(new HierarchyBrowserElementResizeListener(nodesSection, NODE_SECTION_WIDTH_FACTOR, null));
        scrollPanel.setWidth(nodeSectionWidth + "px");
        resizablePanel.addResizeListener(new HierarchyBrowserElementResizeListener(scrollPanel, NODE_SECTION_WIDTH_FACTOR, null));
        nodesSection.addStyleName("node-section");
        scrollPanel.add(nodesSection);
        addNodeLink(linksSection);
        AbsolutePanel buttonsPanel = createFooterButtonPanel();
        linksAndNodesSection.add(linksSection);
        linksAndNodesSection.add(scrollPanel);
        root.add(popupChosenContent);
        root.add(linksAndNodesSection);
        root.add(buttonsPanel);
        /*
         * root.setSize("100%", "100%");
         * root.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
         */
        root.addStyleName("mainPopupContent");
        resizablePanel.wrapWidget(root);

        resizablePanel.addResizeListener(new PanelResizeListener() {
            @Override
            public void onPanelResize(int width, int height) {
                root.setSize(width + MAIN_CONTENT_MARGIN + "px", height + "px");
                nodeSectionWidth = NODE_SECTION_WIDTH_FACTOR * width;
                nodeSectionHeight = NODE_VIEW_HEIGHT_FACTOR * height;
                dialogBox.getElement().getFirstChildElement().getFirstChildElement().getStyle().clearHeight();// ugly,
                                                                                                              // should
                                                                                                              // be
                                                                                                              // changed
                dialogBox.getElement().getFirstChildElement().getFirstChildElement().getStyle().clearWidth();
                adjustNodeWidth();
            }
        });
        return resizablePanel;
    }

    public void addNodeLink(VerticalPanel linksSection) {
        HorizontalPanel nodePanel = new HorizontalPanel();
        nodePanel.addStyleName("selected-node-link");
        linkLabel = new HyperLinkWithHistorySupport();

        linkLabel.setStyleName("node-link");

        linkLabel.setText(title);
        // linkLabel.getElement().getStyle().setColor("white");
        Image arrow = new Image("images/arrow-right.png");
        arrow.setStyleName("link-arrow");
        nodePanel.add(linkLabel);
        nodePanel.add(arrow);
        linksSection.add(nodePanel);

    }

    public void displayChosenItems(List<HierarchyBrowserItem> chosenItems, boolean shouldDisplayTooltipButton) {
        popupChosenContent.displayChosenItems(chosenItems, shouldDisplayTooltipButton);
    }

    public void createAndShowPopup(List<HierarchyBrowserItem> chosenItems) {
        dialogBox = new DialogBox();

        dialogBox.setAnimationEnabled(true);
        dialogBox.setStyleName("popup-body");
        dialogBox.setModal(true);
        dialogBox.setWidget(initPopup());
        dialogBox.getElement().getStyle().setZIndex(1000); // Должен быть выше,
                                                           // чем у
                                                           // ExtSearchDialogBox
        popupChosenContent.setTooltipClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserShowTooltipEvent(popupChosenContent));
            }
        });

        HTML caption = (HTML) dialogBox.getCaption();
        captionCloseButton = new CaptionCloseButton();
        caption.getElement().appendChild(captionCloseButton.getElement());
        popupChosenContent.displayChosenItems(chosenItems, shouldDrawTooltipButton);
        dialogBox.setHeight(popupHeight + "px");
        dialogBox.setWidth(popupWidth + "px");
        dialogBox.center();
    }

    private AbsolutePanel createFooterButtonPanel() {
        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.setStyleName("bottom-popup-buttons-panel");
        okButton = new Button(LocalizeUtil.get(DONE_BUTTON_KEY, DONE_BUTTON));
        okButton.removeStyleName("gwt-Button");
        okButton.addStyleName("lightButton");
        cancelButton = new Button(LocalizeUtil.get(CANCELLATION_BUTTON_KEY,
                CANCELLATION_BUTTON));
        cancelButton.removeStyleName("gwt-Button");
        cancelButton.addStyleName("lightButton");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        return buttonsPanel;
    }

    private String buildNodeType(List<NodeCollectionDefConfig> nodeConfigs) {
        StringBuilder sb = new StringBuilder();
        for (NodeCollectionDefConfig config : nodeConfigs) {
            sb.append(config.getCollection());
            sb.append(config.getRecursiveDeepness());
            sb.append(";");
        }

        return sb.toString();
    }

    public void drawNewNode(Id parentId, String parentCollectionName, List<HierarchyBrowserItem> items,
            List<NodeCollectionDefConfig> nodeConfigs) {
        String nodeType = buildNodeType(nodeConfigs);
        if (containerMap.containsKey(nodeType)) {
            nodesSection.remove(containerMap.get(nodeType));
        }
        if (!nodeTypes.contains(nodeType)) {
            nodeTypes.add(nodeType);
        } else {
            int index = nodeTypes.indexOf(nodeType);
            List<String> children = nodeTypes.subList(index + 1, nodeTypes.size());
            for (String childType : children) {
                HierarchyBrowserNodeView view = containerMap.get(childType);
                if (view != null) {
                    nodesSection.remove(view);
                    containerMap.remove(childType);
                }
            }
        }

        HierarchyBrowserNodeView nodeView = new HierarchyBrowserNodeView(eventBus, (int) nodeSectionHeight, displayAsHyperlinks);

        resizablePanel.addResizeListener(nodeView.getPanelResizeListener());
        nodesSection.add(nodeView);
        nodeView.drawNode(parentId, parentCollectionName, items, nodeConfigs);
        containerMap.put(nodeType, nodeView);
        adjustNodeWidth();

    }

    public void redrawNodeWithMoreItems(List<NodeCollectionDefConfig> nodeConfigs, List<HierarchyBrowserItem> items) {
        String nodeType = buildNodeType(nodeConfigs);
        HierarchyBrowserNodeView nodeView = containerMap.get(nodeType);
        nodeView.drawMoreItems(items);

    }

    public void redrawNode(List<NodeCollectionDefConfig> nodeConfigs, List<HierarchyBrowserItem> items) {
        String nodeType = buildNodeType(nodeConfigs);
        int index = nodeTypes.indexOf(nodeType);
        List<String> children = nodeTypes.subList(index + 1, nodeTypes.size());
        for (String childType : children) {
            HierarchyBrowserNodeView view = containerMap.get(childType);
            if (view != null) {
                nodesSection.remove(view);
                containerMap.remove(childType);
            }
        }
        HierarchyBrowserNodeView nodeView = containerMap.get(nodeType);
        nodeView.redrawNode(items);
        adjustNodeWidth();
    }

    public void addOkClickHandler(ClickHandler openButtonClickHandler) {
        okButton.addClickHandler(openButtonClickHandler);
    }

    public void addLinkClickHandler(ClickHandler linkClickHandler) {
        linkLabel.addDomHandler(linkClickHandler, ClickEvent.getType());
    }

    public void addCancelClickHandler(ClickHandler cancelClickHandler) {
        cancelButton.addClickHandler(cancelClickHandler);
    }

    public void addCancelListener(EventListener listener) {
        captionCloseButton.addClickListener(listener);
    }

    private void adjustNodeWidth() {
        Set<String> keys = containerMap.keySet();
        int size = keys.size();
        if (size == 0) {
            return;
        }
        if (size >= 3) {
            size = 3;
        }
        double oneNodeWidthInPercentage = nodeSectionWidth / size - 5;
        for (String key : keys) {
            HierarchyBrowserNodeView nodeView = containerMap.get(key);
            nodeView.asWidget().getElement().getStyle().setWidth(oneNodeWidthInPercentage, Style.Unit.PX);
        }
    }

    public void refreshNode(HierarchyBrowserItem item) {
        Set<String> nodeTypes = containerMap.keySet();
        for (String nodeType : nodeTypes) {
            containerMap.get(nodeType).refreshNode(item);
        }
    }

    @Override
    public void display(List<HierarchyBrowserItem> items, boolean shouldDrawTooltipButton) {
        popupChosenContent.display(items, shouldDrawTooltipButton);
    }
}
