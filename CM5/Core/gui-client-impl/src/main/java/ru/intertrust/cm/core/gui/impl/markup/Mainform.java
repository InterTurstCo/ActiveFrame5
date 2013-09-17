package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class Mainform extends DockLayoutPanel {
    Double showDrag = 2.00;
    Double showSticker = 5.00;
    Double showTree = 15.00;
    MainContainerWithSplitPanel center = new MainContainerWithSplitPanel();

    public Mainform() {


        super(Unit.EM);//px?
        this.addNorth(north, 5.6);
        this.addSouth(south, showDrag);
        this.addWest(west, 6);
        this.addWest(tree, showTree);
        this.addEast(east, showSticker);

        this.add(center);

        north.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        north.getElement().getStyle().setProperty("margin", "5px");

        south.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        south.getElement().getStyle().setProperty("margin", "5px");

        west.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        west.getElement().getStyle().setProperty("marginLeft", "5px");
        west.getElement().getStyle().setProperty("marginRight", "5px");

        tree.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        tree.getElement().getStyle().setProperty("marginRight", "5px");

        east.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        east.getElement().getStyle().setProperty("marginLeft", "5px");
        east.getElement().getStyle().setProperty("marginRight", "5px");

    }

    CmjHeader north = new CmjHeader();
    FlowPanel south = new FlowPanel();
    FlowPanel east = new FlowPanel();

    CmjNavigation west = new CmjNavigation();
    CmjTree tree = new CmjTree();

    void showDrag() {
        if (this.showDrag == 12.00) {
            this.showDrag = 2.00;

            this.setWidgetSize(south, showDrag);
        }
        else {
            this.showDrag = 12.00;

            this.setWidgetSize(south, showDrag);
        }
    }

    void showSticker() {
        if (this.showDrag == 15.00) {
            this.showDrag = 5.00;

            this.setWidgetSize(east, showDrag);
        }
        else {
            this.showDrag = 15.00;

            this.setWidgetSize(east, showDrag);
        }
    }

    void showTree() {
        if (this.showTree == 15.00) {
            this.showTree = 5.00;

            this.setWidgetSize(tree, showTree);
        }
        else {
            this.showTree = 15.00;

            this.setWidgetSize(tree, showTree);
        }
    }

    void addElementInHeader(Widget element) {
        this.north.add(element);
    }

    void addElementInDragPanel(Widget element) {
        this.south.add(element);
    }

    void addElementInNavigation(Widget element) {
        this.west.add(element);
    }

    void addElementInSticker(Widget element) {
        this.east.add(element);
    }

    void addElementInCenter(Widget element) {
        this.center.add(element);
    }

    void addElementInTree(Widget element) {
        this.tree.add(element);
    }
}
