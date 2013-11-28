package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
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
    private FlowPanel formFlowPanel = new FlowPanel();
    private SimplePanel splitterFirstWidget = new SimplePanel();
    private ScrollPanel splitterScroll = new ScrollPanel();
    private DomainObjectSurferPlugin domainObjectSurferPlugin;
    //локальная шина событий
    private EventBus eventBus;
    private SplitterEx splitterPanel;
    private static Logger log = Logger.getLogger("DomainObjectSurfer");
    private FlowPanel flowPanel;

    public DomainObjectSurferPluginView(Plugin plugin) {
        super(plugin);
        domainObjectSurferPlugin = (DomainObjectSurferPlugin) plugin;
        splitterScroll.getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
        surferWidth = plugin.getOwner().getPanelWidth();
        surferHeight = plugin.getOwner().getPanelHeight();
        initSplitter();
        splitterSetSize();
        eventBus = domainObjectSurferPlugin.getLocalPluginEventBus();
        addSplitterWidgetResizeHandler();

    }

    private void initSplitter() {
        splitterPanel = new SplitterEx(9, domainObjectSurferPlugin.getLocalPluginEventBus()) {
            @Override
            public void onResize() {
                super.onResize();
                eventBus
                        .fireEvent(new SplitterInnerScrollEvent(splitterScroll.getOffsetHeight(),
                                splitterScroll.getOffsetWidth(), formFlowPanel.getOffsetHeight(),
                                formFlowPanel.getOffsetWidth()));

            }
        };
    }

    public void onPluginPanelResize() {
        updateSizes();
        splitterSetSize();
    }

    private void updateSizes() {
        surferWidth = plugin.getOwner().getPanelWidth();
        surferHeight = plugin.getOwner().getPanelHeight();

    }

    protected void splitterSetSize() {
        splitterPanel.clear();

        splitterPanel.setSize(surferWidth + "px", surferHeight + "px");
        splitterPanel.addNorth(splitterScroll, surferHeight / 2);
        splitterPanel.add(formFlowPanel);
    }

    private void addSplitterWidgetResizeHandler() {
        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {
            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {

                if (event.isType()) {
                    splitterPanel.remove(0);
                    splitterPanel.insertWest(splitterScroll, event.getFirstWidgetWidth(), splitterPanel.getWidget(0));
                } else {
                    splitterPanel.remove(0);
                    splitterPanel.insertNorth(splitterScroll, event.getFirstWidgetHeight(), splitterPanel.getWidget(0));
                }
            }
        });

    }

    @Override
    protected IsWidget getViewWidget() {

        flowPanel = new FlowPanel();
        flowPanel.setStyleName("centerTopBottomDividerRoot");
        final VerticalPanel container = new VerticalPanel();
        flowPanel.add(container);
        splitterScroll.add(splitterFirstWidget);

        container.add(splitterPanel);

        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();
        if (config != null) {
            log.info("plugin config, collection = " + config.getCollectionViewerConfig().getCollectionRefConfig().getName());
            final PluginPanel formPluginPanel = new PluginPanel();
            formPluginPanel.setPanelHeight(surferHeight / 2);
            formPluginPanel.setPanelWidth(surferWidth);
            final Plugin collectionViewerPlugin = domainObjectSurferPlugin.getCollectionPlugin();
            collectionViewerPlugin.setConfig(config.getCollectionViewerConfig());


            PluginPanel collectionViewerPluginPanel = new PluginPanel() {
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
                    final FormPlugin formPlugin = (FormPlugin)domainObjectSurferPlugin.getFormPlugin();
                    domainObjectSurferPlugin.setFormPlugin(formPlugin);
                    formPlugin.setConfig(formPluginConfig);

                    formPluginPanel.open(formPlugin);
                    splitterFirstWidget.add(this.asWidget());

                    formFlowPanel.setStyleName("tab-content");
                    formFlowPanel.setSize("100%", "100%");
                    formFlowPanel.add(new ScrollPanel(formPluginPanel.asWidget()));


                }
            };
            collectionViewerPluginPanel.setPanelWidth(surferWidth);
            collectionViewerPluginPanel.setPanelHeight(surferHeight / 2);
            collectionViewerPluginPanel.open(collectionViewerPlugin);

        }
        return flowPanel;
    }

}
