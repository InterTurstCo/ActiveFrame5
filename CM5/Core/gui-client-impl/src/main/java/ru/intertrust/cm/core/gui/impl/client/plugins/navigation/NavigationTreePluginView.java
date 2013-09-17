package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;

public class NavigationTreePluginView extends PluginView {

    protected NavigationTreePluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected IsWidget getViewWidget() {

        VerticalPanel chapterMenu = new VerticalPanel();

        Image inboxImage = stylifyButtonImage(new Image("images/inbox.png"));

        inboxImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

            }
        });


        chapterMenu.add(inboxImage);
        chapterMenu.add(stylifyButtonImage(new Image("images/tasks.png")));
        chapterMenu.add(stylifyButtonImage(new Image("images/calendar.png")));
        chapterMenu.add(stylifyButtonImage(new Image("images/docs.png")));
        chapterMenu.add(stylifyButtonImage(new Image("images/cases.png")));
        chapterMenu.add(stylifyButtonImage(new Image("images/helpers.png")));
        chapterMenu.add(stylifyButtonImage(new Image("images/analitika.png")));

        chapterMenu.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        chapterMenu.getElement().getStyle().setProperty("marginLeft", "5px");
        chapterMenu.getElement().getStyle().setProperty("marginRight", "5px");

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(chapterMenu);

        VerticalPanel navigationTreePanel = new VerticalPanel();
        navigationTreePanel.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        navigationTreePanel.getElement().getStyle().setProperty("marginRight", "5px");

        TreeItem all = new TreeItem();
        all.setText("Все");

        TreeItem allItem = new TreeItem(new Label("Все"));
        TreeItem allIitem2 = new TreeItem(new Label("Все"));
        TreeItem allIitem3 = new TreeItem(new Label("Все"));
        all.addItem(allItem);
        all.addItem(allIitem2);
        all.addItem(allIitem3);

        Tree mainTree = new Tree();
        mainTree.addItem(all);
        mainTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                log.info(event.getSelectedItem().getText());
                plugin.getEventBus().fireEventFromSource(new NavigationTreeItemSelectedEvent(), plugin);
            }
        });
        navigationTreePanel.add(mainTree);
        horizontalPanel.add(navigationTreePanel);

        return horizontalPanel;

    }

    private Image stylifyButtonImage(Image element) {
        element.getElement().getStyle().setProperty("marginLeft", "5px");// ?
        element.getElement().getStyle().setProperty("borderStyle", "solid");
        element.getElement().getStyle().setProperty("borderWidth", "1");
        element.getElement().getStyle().setProperty("borderColor", "BLACK");
        return element;
    }
}
