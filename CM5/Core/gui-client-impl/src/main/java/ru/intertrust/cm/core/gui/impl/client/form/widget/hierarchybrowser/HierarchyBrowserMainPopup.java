package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.DialogWindowConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HyperLinkWithHistorySupport;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserMainPopup {

    public static final int DEFAULT_WIDTH = 1000;
    public static final int DEFAULT_HEIGHT = 400;
    private HierarchyBrowserItemsView popupChosenContent;
    private HorizontalPanel nodesSection;
    private DialogBox dialogBox;
    private int popupWidth;
    private int popupHeight;
    private HyperLinkWithHistorySupport linkLabel;

    private EventBus eventBus;
    private Button okButton;
    private Map<String, HierarchyBrowserNodeView> containerMap;
    private List<String> nodeTypes;
    private SelectionStyleConfig selectionStyleConfig;
    private boolean displayAsHyperlinks;
    private Button cancelButton;
    private double nodeSectionWidth;
    private String title;
    private boolean shouldDrawTooltipButton;
    private String hyperlinkPopupTitle;

    public HierarchyBrowserMainPopup(EventBus eventBus, HierarchyBrowserWidgetState state) {
        this.eventBus = eventBus;

        this.selectionStyleConfig = state.getHierarchyBrowserConfig().getSelectionStyleConfig();
        this.displayAsHyperlinks = HierarchyBrowserUtil.isDisplayingHyperlinks(state);
        this.shouldDrawTooltipButton = state.isTooltipAvailable();
        title = state.getRootNodeLinkConfig() == null ? "link" : state.getRootNodeLinkConfig().getTitle();
        this.hyperlinkPopupTitle = state.getHyperlinkPopupTitle();
        containerMap = new HashMap<String, HierarchyBrowserNodeView>();
        nodeTypes = new ArrayList<String>();

        DialogWindowConfig dialogWindowConfig = state.getHierarchyBrowserConfig().getDialogWindowConfig();
        popupWidth = HierarchyBrowserUtil.getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getWidth() : null, HierarchyBrowserMainPopup.DEFAULT_WIDTH);
        popupHeight = HierarchyBrowserUtil.getSizeFromString(dialogWindowConfig != null ?
                dialogWindowConfig.getHeight() : null, HierarchyBrowserMainPopup.DEFAULT_HEIGHT);

    }

    public void hidePopup() {
        dialogBox.hide();
    }

    private VerticalPanel initPopup() {
        VerticalPanel root = new VerticalPanel();

        HorizontalPanel linksAndNodesSection = new HorizontalPanel();
        linksAndNodesSection.addStyleName("grey-background");
        VerticalPanel linksSection = new VerticalPanel();
        linksSection.setWidth(0.1 * popupWidth + "px");

        popupChosenContent = new HierarchyBrowserItemsView(selectionStyleConfig, eventBus, displayAsHyperlinks,
                hyperlinkPopupTitle);
        popupChosenContent.setHeight(0.13 * popupHeight + "px");
        popupChosenContent.asWidget().addStyleName("popup-chosen-content");
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.addStyleName("node-section-scroll");
        nodesSection = new HorizontalPanel();
        nodeSectionWidth = 0.90 * popupWidth;
        scrollPanel.setWidth(nodeSectionWidth + "px");
        nodesSection.addStyleName("node-section");
        scrollPanel.add(nodesSection);
        addNodeLink(linksSection);
        AbsolutePanel buttonsPanel = createFooterButtonPanel();
        linksAndNodesSection.add(linksSection);
        linksAndNodesSection.add(scrollPanel);
        root.add(popupChosenContent);
        root.add(linksAndNodesSection);
        root.add(buttonsPanel);

        root.setWidth(popupWidth + "px");
        root.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
        return root;
    }

    public void addNodeLink(VerticalPanel linksSection) {
        HorizontalPanel nodePanel = new HorizontalPanel();
        nodePanel.addStyleName("selected-node-link");
        linkLabel = new HyperLinkWithHistorySupport();
        linkLabel.removeStyleName("gwt-Hyperlink ");
        linkLabel.addStyleName("node-link");

        linkLabel.setText(title);
        linkLabel.getElement().getStyle().setColor("white");
        Image arrow = new Image("images/arrow-right.png");
        arrow.removeStyleName("gwt-Image");
        arrow.addStyleName("link-arrow");
        nodePanel.add(linkLabel);
        nodePanel.add(arrow);
        linksSection.add(nodePanel);

    }
    public void displayChosenItems(List<HierarchyBrowserItem> chosenItems, boolean shouldDisplayTooltipButton){
        popupChosenContent.displayChosenItems(chosenItems, shouldDisplayTooltipButton);
    }

    public void createAndShowPopup(List<HierarchyBrowserItem> chosenItems) {
        dialogBox = new DialogBox();

        dialogBox.setAnimationEnabled(true);
        dialogBox.removeStyleName("gwt-DialogBox ");
        dialogBox.addStyleName("popup-body");
        dialogBox.setModal(true);
        dialogBox.add(initPopup());

        popupChosenContent.setTooltipClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserShowTooltipEvent(popupChosenContent));
            }
        });

        popupChosenContent.displayChosenItems(chosenItems, shouldDrawTooltipButton);

        dialogBox.setHeight(popupHeight + "px");
        dialogBox.setWidth(popupWidth + "px");
        dialogBox.center();
    }


    private AbsolutePanel createFooterButtonPanel() {
        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.setStyleName("bottom-popup-buttons-panel");
        okButton = new Button("Готово");
        okButton.removeStyleName("gwt-Button");
        okButton.addStyleName("light-button");
        cancelButton = new Button("Отмена");
        cancelButton.removeStyleName("gwt-Button");
        cancelButton.addStyleName("light-button");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        return buttonsPanel;
    }
    private String buildNodeType(Set<String> nodeTypes){
        StringBuilder sb = new StringBuilder();
        for (String nodeType : nodeTypes) {
            sb.append(nodeType);
            sb.append(";") ;
        }
        return sb.toString();
    }

    public void drawNewNode(Id parentId, String parentCollectionName, List<HierarchyBrowserItem> items,boolean selective,
                            Map<String, String> domainObjectTypesAndTitles) {
        String nodeType = buildNodeType(domainObjectTypesAndTitles.keySet());
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
                if (view != null){
                    nodesSection.remove(view);
                    containerMap.remove(childType);
                }
            }
        }
        HierarchyBrowserNodeView nodeView = new HierarchyBrowserNodeView(eventBus,  (int) (0.6 * popupHeight),
               selective);
        nodesSection.add(nodeView);
        nodeView.drawNode(parentId, parentCollectionName, items, domainObjectTypesAndTitles);
        containerMap.put(nodeType, nodeView);
        adjustNodeWidth();

    }

    public void redrawNodeWithMoreItems(Set<String> domainObjectTypes, List<HierarchyBrowserItem> items) {
        String nodeType = buildNodeType(domainObjectTypes);
        HierarchyBrowserNodeView nodeView = containerMap.get(nodeType);
        nodeView.drawMoreItems(items);

    }

    public void redrawNode(Set<String> domainObjectTypes, List<HierarchyBrowserItem> items) {
        String nodeType = buildNodeType(domainObjectTypes);
        int index = nodeTypes.indexOf(nodeType);
        List<String> children = nodeTypes.subList(index + 1, nodeTypes.size());
        for (String childType : children) {
            HierarchyBrowserNodeView view = containerMap.get(childType);
            if (view != null){
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

    private void adjustNodeWidth(){
        Set<String> keys = containerMap.keySet();
        int size= keys.size();
        if (size == 0){
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

}

