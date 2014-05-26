package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.RootNodeLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserMainPopup {

    public static final int DEFAULT_WIDTH = 1000;
    public static final int DEFAULT_HEIGHT = 400;
    private HierarchyBrowserFacebookStyleView popupChosenContent;
    private HorizontalPanel nodesSection;
    private DialogBox dialogBox;
    private int popupWidth;
    private int popupHeight;
    private Hyperlink linkLabel;
    private int nodeHeight;
    private ArrayList<HierarchyBrowserItem> chosenItems;
    private EventBus eventBus;
    private Button okButton;
    private Map<String, HierarchyBrowserNodeView> containerMap;
    private List<String> nodeTypes;
    private SelectionStyleConfig selectionStyleConfig;
    private boolean displayAsHyperlinks;
    private Button cancelButton;
    private double nodeSectionWidth;
    private RootNodeLinkConfig rootNodeLinkConfig;
    public HierarchyBrowserMainPopup(EventBus eventBus, ArrayList<HierarchyBrowserItem> chosenItems,
                                     int popupWidth, int popupHeight, SelectionStyleConfig selectionStyleConfig,
                                     boolean displayAsHyperlinks, RootNodeLinkConfig rootNodeLinkConfig) {
        this.eventBus = eventBus;
        this.chosenItems = chosenItems;
        this.selectionStyleConfig = selectionStyleConfig;
        this.displayAsHyperlinks = displayAsHyperlinks;
        containerMap = new HashMap<String, HierarchyBrowserNodeView>();
        nodeTypes = new ArrayList<String>();
        this.rootNodeLinkConfig = rootNodeLinkConfig;
        setWidgetSize(popupWidth, popupHeight);
    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return popupChosenContent.getChosenItems();
    }

    public void setChosenItems(ArrayList<HierarchyBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
    }

    public void handleAddingItem(HierarchyBrowserItem item, boolean singleChoice) {
        popupChosenContent.handleAddingItem(item, singleChoice);
    }

    public void handleRemovingItem(HierarchyBrowserItem item) {
        popupChosenContent.handleRemovingItem(item);

    }
    public void handleReplacingChosenItem(HierarchyBrowserItem item) {
        popupChosenContent.handleReplacingChosenItem(item);

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

        popupChosenContent = new HierarchyBrowserFacebookStyleView(selectionStyleConfig, eventBus, displayAsHyperlinks);
        popupChosenContent.asWidget().setHeight(0.1 * popupHeight + "px");
        popupChosenContent.asWidget().addStyleName("popup-chosen-content");
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.addStyleName("node-section-scroll");
//        scrollPanel.getElement().getStyle().setOverflowX(Style.Overflow.SCROLL);
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
        popupChosenContent.handleAddingChosenItems(chosenItems);
        root.setWidth(popupWidth + "px");
      //  root.addStyleName("popup-body");
        root.getElement().getStyle().setMarginRight(10, Style.Unit.PX);
        return root;
    }

    public void addNodeLink(VerticalPanel linksSection) {
        HorizontalPanel nodePanel = new HorizontalPanel();
        nodePanel.addStyleName("selected-node-link");
        linkLabel = new Hyperlink();
        linkLabel.removeStyleName("gwt-Hyperlink ");
        linkLabel.addStyleName("node-link");
        String title = rootNodeLinkConfig == null ? "link" : rootNodeLinkConfig.getTitle();
        linkLabel.setText(title);
        linkLabel.getElement().getStyle().setColor("white");
        Image arrow = new Image("images/arrow-right.png");
        arrow.removeStyleName("gwt-Image");
        arrow.addStyleName("link-arrow");
        nodePanel.add(linkLabel);
        nodePanel.add(arrow);
        linksSection.add(nodePanel);

    }

    public void createAndShowPopup() {
        dialogBox = new DialogBox();
        dialogBox.setAnimationEnabled(true);
        dialogBox.removeStyleName("gwt-DialogBox ");
        dialogBox.addStyleName("popup-body");
        dialogBox.setModal(true);
        dialogBox.add(initPopup());
        dialogBox.setHeight(popupHeight + "px");
        dialogBox.setWidth(popupWidth + "px");
        dialogBox.center();
    }

    public void setWidgetSize(int width, int height) {
        popupWidth = width != 0 ? width : DEFAULT_WIDTH;
        popupHeight = height != 0 ? height : DEFAULT_HEIGHT;
        nodeHeight = (int) (0.6 * popupHeight);
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
        HierarchyBrowserNodeView nodeView = new HierarchyBrowserNodeView(eventBus, nodeHeight,
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

