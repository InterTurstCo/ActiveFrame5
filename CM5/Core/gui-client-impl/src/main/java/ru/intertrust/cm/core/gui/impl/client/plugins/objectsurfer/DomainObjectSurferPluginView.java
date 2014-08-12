package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import java.util.logging.Logger;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
import ru.intertrust.cm.core.gui.impl.client.splitter.SplitterEx;
import ru.intertrust.cm.core.gui.model.action.system.SplitterSettingsActionContext;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;

public class DomainObjectSurferPluginView extends PluginView {
    private static final int SCHEDULE_TIMEOUT = 3000;

    private int surferWidth;
    private int surferHeight;
    private int horizontalSplitterSavedSize = -1;
    private int verticalSplitterSavedSize = -1;
    private FlowPanel sourthRootWidget = new FlowPanel();
    private SimplePanel northRootWidget = new SimplePanel();
    private DomainObjectSurferPlugin domainObjectSurferPlugin;
    //локальная шина событий
    private EventBus eventBus;
    private EventBus globalEventBus = Application.getInstance().getEventBus();
    private SplitterEx dockLayoutPanel;
    private static Logger log = Logger.getLogger("DomainObjectSurfer");
    //private FlowPanel flowPanel;
    private AbsolutePanel rootSurferPanel;
    private Timer timeoutTimer;

    public DomainObjectSurferPluginView(Plugin plugin) {
        super(plugin);
        domainObjectSurferPlugin = (DomainObjectSurferPlugin) plugin;
        surferWidth = plugin.getOwner().getVisibleWidth();
        surferHeight = plugin.getOwner().getVisibleHeight();
        initSplitter();
        sourthRootWidget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        eventBus = domainObjectSurferPlugin.getLocalEventBus();
        addSplitterWidgetResizeHandler();
    }

    private void initSplitter() {
        dockLayoutPanel = new SplitterEx(8, domainObjectSurferPlugin.getLocalEventBus()) {
            @Override
            public void onResize() {
                super.onResize();
                final int splitterSize;
                if (dockLayoutPanel.isSplitType()) {
                    verticalSplitterSavedSize = splitterSize = northRootWidget.getOffsetWidth();
                } else {
                    horizontalSplitterSavedSize = splitterSize = northRootWidget.getOffsetHeight();
                }
                storeSplitterSettings(dockLayoutPanel.isSplitType(), splitterSize);
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
        surferHeight = plugin.getOwner().getVisibleHeight() - (getActionToolBar().getOffsetHeight() + 11);
    }

    protected void splitterSetSize() {
        dockLayoutPanel.setSize(surferWidth + "px", surferHeight + "px");

    }

    private void addSplitterWidgetResizeHandler() {
        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {
            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                checkLastSplitterPosition(event.isType(), event.getFirstWidgetWidth(), event.getFirstWidgetHeight(),
                        event.isArrowsPress());
                final int size = event.isType() ? event.getFirstWidgetWidth() : event.getFirstWidgetHeight();
                storeSplitterSettings(event.isType(), size);
            }
        });
    }

    private void checkLastSplitterPosition(boolean isVertical, int firstWidgetWidth, int firstWidgetHeight, boolean arrowButton) {
        if (!arrowButton) {
            if (horizontalSplitterSavedSize >= 0) {
                firstWidgetHeight = horizontalSplitterSavedSize;
            }
            if (verticalSplitterSavedSize >= 0) {
                firstWidgetWidth = verticalSplitterSavedSize;
                dockLayoutPanel.setSizeFromInsert(firstWidgetWidth);
            }
        }
        if (isVertical && arrowButton) {
            verticalSplitterSavedSize = firstWidgetWidth;
        } else {
            horizontalSplitterSavedSize = firstWidgetHeight;
        }

        reDrawSplitter(isVertical, firstWidgetWidth, firstWidgetHeight);
    }

    private void reDrawSplitter(boolean isVertical, int firstWidgetWidth, int firstWidgetHeight) {
        if (isVertical) {
            dockLayoutPanel.remove(0);
            dockLayoutPanel.insertWest(northRootWidget, firstWidgetWidth, dockLayoutPanel.getWidget(0));
        } else {
            if (firstWidgetHeight > surferHeight) {
                firstWidgetHeight = surferHeight - dockLayoutPanel.getSplitterSize();
            }
            dockLayoutPanel.remove(0);
            dockLayoutPanel.insertNorth(northRootWidget, firstWidgetHeight - dockLayoutPanel.getSplitterSize(), dockLayoutPanel.getWidget(0));
        }
    }

    @Override
    public IsWidget getViewWidget() {
        rootSurferPanel = new AbsolutePanel();
        rootSurferPanel.setStyleName("centerTopBottomDividerRoot");
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("centerTopBottomDividerRootInnerDiv");
        rootSurferPanel.add(container);

        container.add(dockLayoutPanel);
        final DomainObjectSurferPluginData initialData = plugin.getInitialData();
        final boolean isVertical = Integer.valueOf(1).equals(initialData.getSplitterOrientation());
        final Integer splitterSize = initialData.getSplitterPosition();
        if (isVertical) {
            dockLayoutPanel.addWest(northRootWidget, splitterSize == null ? surferWidth / 2 : splitterSize);
        } else {
            dockLayoutPanel.addNorth(northRootWidget, splitterSize == null ? surferHeight / 2 : splitterSize);
        }
        dockLayoutPanel.add(sourthRootWidget);
        splitterSetSize();

        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();

        if (config != null) {
            log.info("plugin config, collection = " + config.getCollectionViewerConfig().getCollectionRefConfig().getName());

            PluginPanel collectionViewerPluginPanel = new PluginPanel();
            collectionViewerPluginPanel.setVisibleWidth(surferWidth);
            collectionViewerPluginPanel.setVisibleHeight(surferHeight / 2);
            collectionViewerPluginPanel.open(domainObjectSurferPlugin.getCollectionPlugin());

            final PluginPanel formPluginPanel = new PluginPanel();
            formPluginPanel.setVisibleWidth(surferWidth);
            formPluginPanel.setVisibleHeight(surferHeight / 2);
            formPluginPanel.open(domainObjectSurferPlugin.getFormPlugin());
            formPluginPanel.asWidget().addStyleName("form-container");

            northRootWidget.add(collectionViewerPluginPanel);
            sourthRootWidget.add(formPluginPanel);
        }
        Application.getInstance().hideLoadingIndicator();
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, plugin.getClass().getSimpleName());
        return rootSurferPanel;
    }

    private void storeSplitterSettings(final boolean vertical, final int size) {
        if (timeoutTimer == null) {
            timeoutTimer = new Timer() {
                @Override
                public void run() {
                    timeoutTimer.cancel();
                    timeoutTimer = null;
                    final ActionConfig actionConfig = new ActionConfig();
                    actionConfig.setImmediate(true);
                    actionConfig.setDirtySensitivity(false);
                    final SplitterSettingsActionContext actionContext = new SplitterSettingsActionContext();
                    actionContext.setOrientation(vertical ? Long.valueOf(1) : Long.valueOf(0));
                    actionContext.setPosition(Long.valueOf(size));
                    actionContext.setActionConfig(actionConfig);
                    final Action action = ComponentRegistry.instance.get(SplitterSettingsActionContext.COMPONENT_NAME);
                    action.setInitialContext(actionContext);
                    action.perform();
                }
            };
        } else {
            timeoutTimer.cancel();
        }
        timeoutTimer.schedule(SCHEDULE_TIMEOUT);
    }
}
