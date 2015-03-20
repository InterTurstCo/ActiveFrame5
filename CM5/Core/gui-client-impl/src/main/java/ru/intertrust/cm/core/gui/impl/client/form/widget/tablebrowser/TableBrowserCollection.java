package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.PanelResizeListener;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.tablebrowser.OpenCollectionRequestEvent;
import ru.intertrust.cm.core.gui.impl.client.panel.ResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.panel.RightSideResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;

import static ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser.TableBrowserViewsBuilder.MINIMAL_TABLE_HEIGHT;
import static ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser.TableBrowserViewsBuilder.MINIMAL_TABLE_WIDTH;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.11.2014
 *         Time: 7:30
 */
public class TableBrowserCollection extends Composite {
    private static int HEIGHT_RESIZE_OFFSET = 10;
    private EventBus eventBus;
    private ResizablePanel root;
    private Boolean displayOnlyChosenIds;
    private Boolean displayCheckBoxes;

    private PluginPanel pluginPanel;
    private Panel pluginWrapper;
    private int height;
    private boolean resizable;

    public TableBrowserCollection() {
        this.root = new RightSideResizablePanel(MINIMAL_TABLE_WIDTH, MINIMAL_TABLE_HEIGHT, true);

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
    public TableBrowserCollection withResizable(boolean resizable){
        this.resizable = resizable;
        return this;
    }

    public TableBrowserCollection createView(){
        root.addStyleName("tableWrapper");

        pluginWrapper = new AbsolutePanel();
        pluginWrapper.setStyleName("collectionPluginWrapper");
        pluginWrapper.getElement().getStyle().setHeight(height, Style.Unit.PX);
        pluginWrapper.add(pluginPanel);
        root.setResizable(resizable);
        root.wrapWidget(pluginWrapper);
        root.addResizeListener(new PanelResizeListener() {
            @Override
            public void onPanelResize(int width, int height) {
                CollectionPluginView view = (CollectionPluginView) pluginPanel.getCurrentPlugin().getView();
                view.resetBodyHeight();
                pluginWrapper.getElement().getStyle().setHeight(height - HEIGHT_RESIZE_OFFSET, Style.Unit.PX);

            }
        });
        return this;
    }


   public void init(){
       OpenCollectionRequestEvent event = new OpenCollectionRequestEvent(pluginPanel, displayOnlyChosenIds,
               displayCheckBoxes);
       eventBus.fireEvent(event);

   }

}
