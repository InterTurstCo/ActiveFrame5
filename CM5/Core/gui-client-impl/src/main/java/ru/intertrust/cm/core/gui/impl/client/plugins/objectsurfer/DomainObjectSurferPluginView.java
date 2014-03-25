package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
import ru.intertrust.cm.core.gui.impl.client.splitter.SplitterEx;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.logging.Logger;

public class DomainObjectSurferPluginView extends PluginView {

    private int surferWidth;
    private int surferHeight;
    private int horizontalSplitterSavedSize = -1;
    private int verticalSplitterSavedSize = -1;
    private FlowPanel formFlowPanel = new FlowPanel();
    private SimplePanel splitterFirstWidget = new SimplePanel();
    private DomainObjectSurferPlugin domainObjectSurferPlugin;
    //локальная шина событий
    private EventBus eventBus;
    private EventBus globalEventBus = Application.getInstance().getEventBus();
    private SplitterEx splitterPanel;
    private static Logger log = Logger.getLogger("DomainObjectSurfer");
    //private FlowPanel flowPanel;
    private AbsolutePanel flowPanel;

    public DomainObjectSurferPluginView(Plugin plugin) {
        super(plugin);
        domainObjectSurferPlugin = (DomainObjectSurferPlugin) plugin;
        surferWidth = plugin.getOwner().getVisibleWidth();
        surferHeight = plugin.getOwner().getVisibleHeight();
        initSplitter();
        splitterPanel.addNorth(splitterFirstWidget, surferHeight / 2);
        splitterPanel.add(formFlowPanel);
        splitterSetSize();
        formFlowPanel.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        eventBus = domainObjectSurferPlugin.getLocalEventBus();


        addSplitterWidgetResizeHandler();

    }

    private void initSplitter() {
        splitterPanel = new SplitterEx(8, domainObjectSurferPlugin.getLocalEventBus()) {
            @Override
            public void onResize() {
                super.onResize();
                eventBus.fireEvent(new SplitterInnerScrollEvent(splitterFirstWidget.getOffsetHeight(),
                        splitterFirstWidget.getOffsetWidth(), formFlowPanel.getOffsetHeight(),
                        formFlowPanel.getOffsetWidth(), splitterPanel.isSplitType()));

                if (!splitterPanel.isSplitType()) {
                    horizontalSplitterSavedSize = splitterFirstWidget.getOffsetHeight();
                }
                if (splitterPanel.isSplitType()) {
                    verticalSplitterSavedSize = splitterFirstWidget.getOffsetWidth();
                }
            }
        };
    }


    public void onPluginPanelResize() {
        updateSizes();
        splitterSetSize();
    }

    private void updateSizes() {
        surferWidth = plugin.getOwner().getVisibleWidth();
            // -11 px  из BusinessUniverse отражаются на размере плагина и выражены как высота actionBar + 11 px
        surferHeight = plugin.getOwner().getVisibleHeight()-(getActionToolBar().getOffsetHeight()+11);
    }

    protected void splitterSetSize() {
        splitterPanel.setSize(surferWidth + "px", surferHeight + "px");
        checkLastSplitterPosition(splitterPanel.isSplitType(), surferWidth, surferHeight / 2, false);

    }

    private void addSplitterWidgetResizeHandler() {
        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {
            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                checkLastSplitterPosition(event.isType(), event.getFirstWidgetWidth(), event.getFirstWidgetHeight(), event.isArrowsPress());

            }
        });

    }

    private void checkLastSplitterPosition(boolean type, int firstWidgetWidth, int firstWidgetHeight, boolean arrowButton) {
        if (!arrowButton) {
            if (horizontalSplitterSavedSize >= 0) {
                firstWidgetHeight = horizontalSplitterSavedSize;
            }

            if (verticalSplitterSavedSize >= 0) {
                firstWidgetWidth = verticalSplitterSavedSize;
                splitterPanel.setSizeFromInsert(firstWidgetWidth);

            }
        }

        if (type && arrowButton) {
            verticalSplitterSavedSize = firstWidgetWidth;

        } else {
            horizontalSplitterSavedSize = firstWidgetHeight;
        }

        reDrawSplitter(type, firstWidgetWidth, firstWidgetHeight);

    }

    private void reDrawSplitter(boolean type, int firstWidgetWidth, int firstWidgetHeight) {

        if (type) {

            if (firstWidgetWidth > surferWidth) {
                firstWidgetWidth = surferWidth - splitterPanel.getSplitterSize();

            }

            splitterPanel.remove(0);
            splitterPanel.insertWest(splitterFirstWidget, firstWidgetWidth, splitterPanel.getWidget(0));

        } else {

            if (firstWidgetHeight > surferHeight) {
                firstWidgetHeight = surferHeight - splitterPanel.getSplitterSize();
            }

            splitterPanel.remove(0);
            splitterPanel.insertNorth(splitterFirstWidget, firstWidgetHeight - splitterPanel.getSplitterSize(), splitterPanel.getWidget(0));


        }
    }

    @Override
    public IsWidget getViewWidget() {
        Application.getInstance().enableTimer();
        flowPanel = new AbsolutePanel();
        flowPanel.setStyleName("centerTopBottomDividerRoot");
        //final VerticalPanel container = new VerticalPanel();
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("centerTopBottomDividerRootInnerDiv");
        flowPanel.add(container);

        container.add(splitterPanel);

        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();

        if (config != null) {
            log.info("plugin config, collection = " + config.getCollectionViewerConfig().getCollectionRefConfig().getName());
            final PluginPanel formPluginPanel = new PluginPanel();
            formPluginPanel.setVisibleHeight(surferHeight / 2);
            formPluginPanel.setVisibleWidth(surferWidth);
            final Plugin collectionViewerPlugin = domainObjectSurferPlugin.getCollectionPlugin();
            collectionViewerPlugin.setConfig(config.getCollectionViewerConfig());


            PluginPanel collectionViewerPluginPanel  = new PluginPanel() {
                @Override
                public void beforePluginOpening() {
                    CollectionPluginData collectionPluginData = collectionViewerPlugin.getInitialData();
                    ArrayList<CollectionRowItem> items = collectionPluginData.getItems();
                    FormPluginConfig formPluginConfig;
                    if (items == null || items.size() == 0) {
                        // TODO: New approach needed, collection type is no longer available
                        // open empty form for collection domain object type
                        // config = new SomeActivePluginConfig(collectionPluginData.getCollectionConfig()
                        // .getDomainObjectType());
                        formPluginConfig = new FormPluginConfig(config.getDomainObjectTypeToCreate());
                    } else {
                        formPluginConfig = new FormPluginConfig(items.get(0).getId());
                    }
                    FormPlugin formPlugin = (FormPlugin) domainObjectSurferPlugin.getFormPlugin();

                    formPluginPanel.open(formPlugin);

                    //noname div

                    formPluginPanel.asWidget().addStyleName("form-container");

                    splitterFirstWidget.add(this.asWidget());

                    formFlowPanel.add(formPluginPanel.asWidget());

                }
            };
            collectionViewerPluginPanel.setVisibleWidth(surferWidth);
            collectionViewerPluginPanel.setVisibleHeight(surferHeight / 2);
            collectionViewerPluginPanel.open(collectionViewerPlugin);

        }
        Application.getInstance().disableTimer();
        return flowPanel;
    }

}
