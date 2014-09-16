package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.HierarchicalCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
import ru.intertrust.cm.core.gui.impl.client.splitter.SplitterEx;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.system.SplitterSettingsActionContext;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;
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
    private EventBus eventBus;
    private SplitterEx dockLayoutPanel;
    private static Logger log = Logger.getLogger("DomainObjectSurfer");
    //private FlowPanel flowPanel;
    private AbsolutePanel rootSurferPanel;
    private SplitterSettingsTimeoutTimer timeoutTimer;

    public DomainObjectSurferPluginView(Plugin plugin) {
        super(plugin);
        domainObjectSurferPlugin = (DomainObjectSurferPlugin) plugin;
        surferWidth = plugin.getOwner().getVisibleWidth();
        surferHeight = plugin.getOwner().getVisibleHeight();
        initSplitter();
        sourthRootWidget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        eventBus = domainObjectSurferPlugin.getLocalEventBus();
        addSplitterWidgetResizeHandler();
        addExpandHierarchicalCollectionHandler();
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

    private void addExpandHierarchicalCollectionHandler() {
        eventBus.addHandler(HierarchicalCollectionEvent.TYPE, new HierarchicalCollectionEventHandler() {
            @Override
            public void onExpandHierarchyEvent(HierarchicalCollectionEvent event) {
                //FIXME: to implement real logic of the selection correct config
                ChildCollectionViewerConfig childCollectionViewerConfig =  event.getChildCollectionViewerConfigs().get(0);

                CollectionViewerConfig collectionViewerConfig = childCollectionViewerConfig.getCollectionViewerConfig();
                collectionViewerConfig.setHierarchical(true);
                collectionViewerConfig.setSelectionFiltersConfig(prepareFilterForHierarchicalCollection(
                        childCollectionViewerConfig.getFilter(), event.getSelectedId()));
                DomainObjectSurferConfig domainObjectSurferConfig = new DomainObjectSurferConfig();
                domainObjectSurferConfig.setCollectionViewerConfig(collectionViewerConfig);
                domainObjectSurferConfig.setDomainObjectTypeToCreate(childCollectionViewerConfig.getDomainObjectTypeToCreate());

                final Command command = new Command("initialize", "domain.object.surfer.plugin", domainObjectSurferConfig);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ApplicationWindow.errorAlert(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        DomainObjectSurferPluginData pluginData = (DomainObjectSurferPluginData) result;
                        pluginData.getCollectionPluginData().setExpandHierarchyMarker(true);
                        domainObjectSurferPlugin.setInitialData(pluginData);
                        Application.getInstance().getEventBus().fireEvent(new CentralPluginChildOpeningRequestedEvent
                                (domainObjectSurferPlugin));
                    }
                });
            }
        });
    }

    private SelectionFiltersConfig prepareFilterForHierarchicalCollection(String filter, Id selectedId) {
        AbstractFilterConfig initFilterConfig = new InitialFilterConfig();
        initFilterConfig.setName(filter);
        List<ParamConfig> paramConfigs = new ArrayList<>();
        ParamConfig paramConfig = new ParamConfig();
        paramConfig.setName(0);
        paramConfig.setType(ModelConstants.REFERENCE_TYPE);
        paramConfig.setValue(selectedId.toStringRepresentation());
        paramConfigs.add(paramConfig);
        initFilterConfig.setParamConfigs(paramConfigs);

        SelectionFiltersConfig selectionFiltersConfig = new SelectionFiltersConfig();
        List<AbstractFilterConfig> abstractFilterConfigs = new ArrayList<>();
        abstractFilterConfigs.add(initFilterConfig);
        selectionFiltersConfig.setAbstractFilterConfigs(abstractFilterConfigs);

        return selectionFiltersConfig;
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
}
