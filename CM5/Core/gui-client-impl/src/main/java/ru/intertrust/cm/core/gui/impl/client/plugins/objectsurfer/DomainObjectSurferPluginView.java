package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionDataGrid;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPluginView;
import ru.intertrust.cm.core.gui.impl.client.splitter.SplitterEx;
import ru.intertrust.cm.core.gui.model.action.system.SplitterSettingsActionContext;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.logging.Logger;

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
    private SplitterEx dockLayoutPanel;
    private static Logger log = Logger.getLogger("DomainObjectSurfer");
    //private FlowPanel flowPanel;
    private AbsolutePanel rootSurferPanel;
    private SplitterSettingsTimeoutTimer timeoutTimer;
    private EventBus eventBus;
    private int rowNumber = 0;
    private static int countClick = 0;
    private static Timer timer;

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
        domainObjectSurferPlugin.getCollectionPlugin().setNavigationConfig(domainObjectSurferPlugin.getNavigationConfig());

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
            formPluginPanel.asWidget().addStyleName("formContainer");

            northRootWidget.add(collectionViewerPluginPanel);
            sourthRootWidget.add(formPluginPanel);
        }
        Application.getInstance().hideLoadingIndicator();
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, plugin.getClass().getSimpleName());

        CollectionPluginView collectionPluginView = (CollectionPluginView) domainObjectSurferPlugin.getCollectionPlugin().getView();
        collectionPluginView.getTableBody().addCellPreviewHandler(new CollectionCellPreviewHandler());
        collectionPluginView.getTableBody().sinkEvents(Event.ONDBLCLICK | Event.ONCLICK);

        return rootSurferPanel;
    }

    private void storeSplitterSettings(final boolean vertical, final int size) {
        if (timeoutTimer == null) {
            timeoutTimer = new SplitterSettingsTimeoutTimer(vertical, size);
        } else {
            timeoutTimer.cancel();
            timeoutTimer.setParameters(vertical, size);
        }
        timeoutTimer.schedule(SCHEDULE_TIMEOUT);
    }

    private class SplitterSettingsTimeoutTimer extends Timer {
        private Long orientation;
        private Long size;

        private SplitterSettingsTimeoutTimer(boolean vertical, int size) {
            setParameters(vertical, size);
        }

        public void setParameters(boolean vertical, int size) {
            this.orientation = vertical ? Long.valueOf(1) : Long.valueOf(0);
            this.size = Long.valueOf(size);

        }

        @Override
        public void run() {
            cancel();
            timeoutTimer = null;
            final ActionConfig actionConfig = new ActionConfig();
            actionConfig.setImmediate(true);
            actionConfig.setDirtySensitivity(false);
            final SplitterSettingsActionContext actionContext = new SplitterSettingsActionContext();
            actionContext.setOrientation(orientation);
            actionContext.setPosition(size);
            actionContext.setActionConfig(actionConfig);
            final Action action = ComponentRegistry.instance.get(SplitterSettingsActionContext.COMPONENT_NAME);
            action.setInitialContext(actionContext);
            action.perform();
        }
    }

    private void singleClickMethod(CellPreviewEvent<CollectionRowItem> event) {
        Id id = event.getValue().getId();
        Application.getInstance().getHistoryManager().setSelectedIds(id);
        eventBus.fireEvent(new CollectionRowSelectedEvent(id));
    }

    private void doubleClickMethod(DomainObjectSurferPlugin domainObjectSurferPlugin, CellPreviewEvent<CollectionRowItem> event) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) domainObjectSurferPlugin;
        Id id = event.getValue().getId();
        final FormPluginConfig config;
        if (id == null) {
            config = new FormPluginConfig(editor.getRootDomainObject().getTypeName());
        } else {
            config = new FormPluginConfig(id);
        }
        final FormPluginState state = editor.getFormPluginState();
        config.setPluginState(state);
        state.setEditable(true);
        config.setFormViewerConfig(editor.getFormViewerConfig());

        final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
        formPlugin.setConfig(config);
        formPlugin.setDisplayActionToolBar(true);
        formPlugin.setLocalEventBus(domainObjectSurferPlugin.getLocalEventBus());

        if (state.isInCentralPanel()) {
            domainObjectSurferPlugin.getOwner().closeCurrentPlugin();
        } else {
            state.setInCentralPanel(true);
        }
        Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent(formPlugin));

    }

    private class CollectionCellPreviewHandler implements CellPreviewEvent.Handler<CollectionRowItem> {

        @Override
        public void onCellPreview(final CellPreviewEvent<CollectionRowItem> event) {
            CollectionDataGrid grid = (CollectionDataGrid) event.getSource();
            int row = grid.getKeyboardSelectedRow();

            if (Event.getTypeInt(event.getNativeEvent().getType()) == Event.ONDBLCLICK) {
                doubleClickMethod(domainObjectSurferPlugin, event);
            }

            if (Event.getTypeInt(event.getNativeEvent().getType()) == Event.ONCLICK & rowNumber != row) {
                countClick++;
                rowNumber = row;
                timer = new Timer() {
                    @Override
                    public void run() {
                        if (countClick > 1) {
                            doubleClickMethod(domainObjectSurferPlugin, event);
                        } else {
                            singleClickMethod(event);
                        }
                        countClick = 0;
                    }
                };
                timer.schedule(500);
            }
        }
    }
}
