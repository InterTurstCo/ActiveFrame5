package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.tablebrowser.OpenCollectionRequestEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.11.2014
 *         Time: 7:30
 */
public class TableBrowserCollection extends Composite {

    private EventBus eventBus;
    private AbsolutePanel root;
    private Boolean displayOnlyChosenIds;
    private Boolean displayCheckBoxes;

    private PluginPanel pluginPanel;
    private Panel pluginWrapper;
    private int height;

    public TableBrowserCollection() {
        this.root = new AbsolutePanel();

        initWidget(root);
    }

    public TableBrowserCollection withEventBus(EventBus eventBus){
        this.eventBus = eventBus;
        return this;
    }
    public TableBrowserCollection withPluginPanel(PluginPanel pluginPanel){
        this.pluginPanel = pluginPanel;
        return this;
    }
    public TableBrowserCollection withDisplayOnlyChosenIds(Boolean displayOnlyChosenIds){
        this.displayOnlyChosenIds = displayOnlyChosenIds;
        return this;
    }
    public TableBrowserCollection withDisplayCheckBoxes(Boolean displayCheckBoxes){
        this.displayCheckBoxes = displayCheckBoxes;
        return this;
    }

    public TableBrowserCollection withHeight(int height){
        this.height = height;
        return this;
    }

    public TableBrowserCollection createView(){
        root.addStyleName("tableWrapper");

        pluginWrapper = new AbsolutePanel();
        pluginWrapper.setStyleName("collectionPluginWrapper");
        pluginWrapper.getElement().getStyle().setHeight(height, Style.Unit.PX);
        pluginWrapper.add(pluginPanel);
        root.add(pluginWrapper);

        return this;
    }


   public void init(){
       OpenCollectionRequestEvent event = new OpenCollectionRequestEvent(pluginPanel, displayOnlyChosenIds,
               displayCheckBoxes);
       eventBus.fireEvent(event);

   }

}
