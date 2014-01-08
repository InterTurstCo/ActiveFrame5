package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserMainPopup {

    private HierarchyBrowserFacebookStyleView popupChosenContent;
    private HorizontalPanel nodesSection;
    private PopupPanel popup;
    private int popupWidth;
    private int popupHeight;
    private Hyperlink linkLabel;
    private int nodeHeight;
    private ArrayList<HierarchyBrowserItem> chosenItems;
    private EventBus eventBus;
    private Button okButton;
    private Map<String, HierarchyBrowserNodeView> containerMap;
    private List<String> nodeTypes;

    public HierarchyBrowserMainPopup(EventBus eventBus, ArrayList<HierarchyBrowserItem> chosenItems,
                                     int popupWidth, int popupHeight) {
        this.eventBus = eventBus;
        this.chosenItems = chosenItems;
        containerMap = new HashMap<String, HierarchyBrowserNodeView>();
        nodeTypes = new ArrayList<String>();
        setWidgetSize(popupWidth, popupHeight);
    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return popupChosenContent.getChosenItems();
    }

    public void handleAddingChosenItem(HierarchyBrowserItem item) {
        popupChosenContent.handleAddingChosenItem(item);
    }

    public void handleRemovingChosenItem(HierarchyBrowserItem item) {
        popupChosenContent.handleRemovingChosenItem(item);

    }

    public void hidePopup() {
        popup.hide();
    }

    private VerticalPanel initPopup() {
        VerticalPanel root = new VerticalPanel();
        Label title = new Label("Выбрать");
        title.addStyleName("header-title");
        root.add(title);
        HorizontalPanel linksAndNodesSection = new HorizontalPanel();
        VerticalPanel linksSection = new VerticalPanel();
        linksSection.setWidth(0.2 * popupWidth + "px");
        linksSection.addStyleName("grey-background");
        linksSection.setHeight(0.905 * popupHeight + "px");
        popupChosenContent = new HierarchyBrowserFacebookStyleView(eventBus);
        popupChosenContent.asWidget().setHeight(0.1 * popupHeight + "px");
        popupChosenContent.asWidget().addStyleName("popup-chosen-content");
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.addStyleName("node-section-scroll");
        scrollPanel.getElement().getStyle().setOverflowX(Style.Overflow.SCROLL);
        nodesSection = new HorizontalPanel();
        nodesSection.setWidth(0.80 * popupWidth + "px");
        nodesSection.addStyleName("node-section");
        scrollPanel.add(nodesSection);
        addNodeLink("link", linksSection);
        HorizontalPanel buttonsPanel = createFooterButtonPanel();
        linksAndNodesSection.add(linksSection);
        linksAndNodesSection.add(scrollPanel);
        root.add(popupChosenContent);
        root.add(linksAndNodesSection);
        root.add(buttonsPanel);
        popupChosenContent.handleAddingChosenItems(chosenItems);
        root.setWidth(popupWidth + "px");
        root.addStyleName("popup-body");
        return root;
    }

    public void addNodeLink(String title, VerticalPanel linksSection) {
        HorizontalPanel nodePanel = new HorizontalPanel();
        nodePanel.addStyleName("selected-node-link");
        linkLabel = new Hyperlink();

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

    public void createAndShowPopup() {
        popup = new PopupPanel(false);
        popup.removeStyleName("gwt-PopupPanel");
        popup.addStyleName("popup-body");
        popup.getElement().getStyle().setZIndex(10);
        popup.setModal(true);
        popup.add(initPopup());
        popup.setHeight(popupHeight + "px");
        popup.center();
    }

    public void setWidgetSize(int width, int height) {
        popupWidth = width != 0 ? width : 900;
        popupHeight = height != 0 ? height : 400;
        nodeHeight = (int) (0.6 * popupHeight);
    }

    private HorizontalPanel createFooterButtonPanel() {
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setStyleName("bottom-popup-buttons-panel");
        okButton = new Button("Готово");
        okButton.removeStyleName("gwt-Button");
        okButton.addStyleName("bottom-popup-button");
        Button cancelButton = new Button("Отмена");
        cancelButton.removeStyleName("gwt-Button");
        cancelButton.addStyleName("bottom-popup-button");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setHeight(0.2 * popupHeight + "px");
        return buttonsPanel;
    }

    public void drawNewNode(List<HierarchyBrowserItem> items, String nodeType, Id parentId) {
        if (items.isEmpty()) {
            return;
        }
        if (containerMap.containsKey(nodeType)) {
            nodesSection.remove(containerMap.get(nodeType));
        }
        if (!nodeTypes.contains(nodeType)) {
            nodeTypes.add(nodeType);
        } else {
            int index = nodeTypes.indexOf(nodeType);
            List<String> children = nodeTypes.subList(index + 1, nodeTypes.size());
            for (String childType : children) {
                nodesSection.remove(containerMap.get(childType));
            }
        }
        HierarchyBrowserNodeView nodeView = new HierarchyBrowserNodeView(eventBus, nodeHeight, parentId);
        nodeView.setItems(items);
        nodeView.asWidget().setWidth("100%");
        nodesSection.add(nodeView);
        nodeView.drawNode();
        containerMap.put(nodeType, nodeView);

    }

    public void redrawNodeWithMoreItems(List<HierarchyBrowserItem> items, String nodeType) {
        HierarchyBrowserNodeView nodeView = containerMap.get(nodeType);
        nodeView.drawMoreItems(items);

    }
    public void redrawNode(List<HierarchyBrowserItem> items, String nodeType, Id parentId) {
        int index = nodeTypes.indexOf(nodeType);
        List<String> children = nodeTypes.subList(index + 1, nodeTypes.size());
        for (String childType : children) {
            nodesSection.remove(containerMap.get(childType));
        }
        HierarchyBrowserNodeView nodeView = containerMap.get(nodeType);
        nodeView.redrawNode(items, parentId);
    }

    public void addOkClickHandler(ClickHandler openButtonClickHandler) {
        okButton.addClickHandler(openButtonClickHandler);
    }

    public void addLinkClickHandler(ClickHandler linkClickHandler) {
        linkLabel.addDomHandler(linkClickHandler, ClickEvent.getType());
    }
}

