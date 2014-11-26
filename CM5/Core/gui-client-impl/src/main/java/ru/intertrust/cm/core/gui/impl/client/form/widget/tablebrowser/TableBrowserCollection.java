package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.tablebrowser.OpenCollectionRequestEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.11.2014
 *         Time: 7:30
 */
public class TableBrowserCollection extends TableBrowserEditableComposite {

    private EventBus eventBus;

    private Boolean displayOnlySelectedIds;
    private Boolean displayCheckBoxes;
    private Boolean displayFilter;
    private Boolean tooltipLimitation = false;

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
    public TableBrowserCollection withDisplayOnlySelectedIds(Boolean displayOnlySelectedIds){
        this.displayOnlySelectedIds = displayOnlySelectedIds;
        return this;
    }
    public TableBrowserCollection withDisplayCheckBoxes(Boolean displayCheckBoxes){
        this.displayCheckBoxes = displayCheckBoxes;
        return this;
    }
    public TableBrowserCollection withDisplayFilter(Boolean displayFilter){
        this.displayFilter = displayFilter;
        return this;
    }
    public TableBrowserCollection withTooltipLimitation(Boolean tooltipLimitation){
        this.tooltipLimitation = tooltipLimitation;
        return this;
    }
    public TableBrowserCollection withHeight(int height){
        this.height = height;
        return this;
    }

    public TableBrowserCollection createView(){
        root.addStyleName("tableWrapper");
        if(displayFilter){
            initFilter();
        }

        pluginWrapper = new AbsolutePanel();
        pluginWrapper.setStyleName("collectionPluginWrapper");
        pluginWrapper.getElement().getStyle().setHeight(height, Style.Unit.PX);
        pluginWrapper.add(pluginPanel);
        root.add(pluginWrapper);

        return this;
    }


   public void refresh(){
       OpenCollectionRequestEvent event = new OpenCollectionRequestEvent(pluginPanel, displayOnlySelectedIds,
               displayCheckBoxes, tooltipLimitation);
       eventBus.fireEvent(event);

   }

   public void clearContent(){
       OpenCollectionRequestEvent event = new OpenCollectionRequestEvent(pluginPanel,displayOnlySelectedIds, displayCheckBoxes, tooltipLimitation);
       eventBus.fireEvent(event);
   }
    private void initFilter() {
        Panel controlsWrapper = new AbsolutePanel();
        controlsWrapper .setStyleName("tableBrowserControlsWrapper");
        root.add(controlsWrapper );
        filter = new TextBox();
        filter.setStyleName("tableBrowserCollectionFilterInput");
        controlsWrapper .add(filter);
        filter.setFocus(true);
    }

}
