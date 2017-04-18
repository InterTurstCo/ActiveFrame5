package ru.intertrust.cm.core.gui.impl.client.plugins.listplugin;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.listplugin.ListSurferConfig;
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
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.SplitterSettingsActionContext;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListSurferPluginData;

import java.util.logging.Logger;

/**
 * Created by Ravil on 11.04.2017.
 */
public class ListSurferPluginView extends PluginView  {


    private ListSurferPlugin listSurferPlugin;
    private static final int SCHEDULE_TIMEOUT = 3000;
    private int surferWidth;
    private int surferHeight;
    private int horizontalSplitterSavedSize = -1;
    private int verticalSplitterSavedSize = -1;
    private FlowPanel southRootWidget = new FlowPanel();
    private SimplePanel northRootWidget = new SimplePanel();
    private AbsolutePanel rootSurferPanel;
    private SplitterEx dockLayoutPanel;
    private static Logger log = Logger.getLogger("ListSurfer");
    private EventBus eventBus;
    private SplitterSettingsTimeoutTimer timeoutTimer;

    protected ListSurferPluginView(Plugin plugin){
        super(plugin);
        listSurferPlugin = (ListSurferPlugin)plugin;
        surferWidth = plugin.getOwner().getVisibleWidth();
        surferHeight = plugin.getOwner().getVisibleHeight();
        initSplitter();
        southRootWidget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        eventBus = listSurferPlugin.getLocalEventBus();
        addSplitterWidgetResizeHandler();
    }

    @Override
    public IsWidget getViewWidget() {
        rootSurferPanel = new AbsolutePanel();
        rootSurferPanel.setStyleName("centerTopBottomDividerRoot");
        northRootWidget.addStyleName("topPanel");
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("centerTopBottomDividerRootInnerDiv");
        rootSurferPanel.add(container);
        container.add(dockLayoutPanel);
        ListSurferPluginData pluginData = plugin.getInitialData();
        final boolean isVertical = Integer.valueOf(1).equals(pluginData.getSplitterOrientation());
        //final Integer splitterSize = pluginData.getSplitterPosition();
        final Integer splitterSize = 459;
        if (isVertical) {
            dockLayoutPanel.addWest(northRootWidget, splitterSize == null ? surferWidth / 2 : splitterSize);
        } else {
            dockLayoutPanel.addNorth(northRootWidget, splitterSize == null ? surferHeight / 2 : splitterSize - dockLayoutPanel.getSplitterSize());
        }
        splitterSetSize();
        dockLayoutPanel.add(southRootWidget);
        if (splitterSize != null) {
            dockLayoutPanel.setCustomSize(splitterSize);
        }
        final ListSurferConfig config  = (ListSurferConfig)listSurferPlugin.getConfig();
        if (config != null) {
            PluginPanel listPluginPanel = new PluginPanel();
            listPluginPanel.setVisibleWidth(surferWidth);
            listPluginPanel.setVisibleHeight(surferHeight / 2);
            listPluginPanel.open(listSurferPlugin.getListPlugin());

            final PluginPanel formPluginPanel = new PluginPanel();
            formPluginPanel.setVisibleWidth(surferWidth);
            formPluginPanel.setVisibleHeight(surferHeight / 2);
            formPluginPanel.open(listSurferPlugin.getFormPlugin());
            formPluginPanel.asWidget().addStyleName("formContainer");
            northRootWidget.add(listPluginPanel);
            southRootWidget.add(formPluginPanel);
        }
        Application.getInstance().hideLoadingIndicator();
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, plugin.getClass().getSimpleName());
        return rootSurferPanel;
    }


    private void initSplitter() {
        dockLayoutPanel = new SplitterEx(8, listSurferPlugin.getLocalEventBus()) {
            @Override
            public void onResize() {

                super.onResize();
                final int splitterSize;
                if (dockLayoutPanel.isSplitType()) {
                    verticalSplitterSavedSize = splitterSize = northRootWidget.getOffsetWidth();
                    setCustomSize(verticalSplitterSavedSize);
                } else {
                    horizontalSplitterSavedSize = splitterSize = northRootWidget.getOffsetHeight() + dockLayoutPanel.getSplitterSize();
                    setCustomSize(horizontalSplitterSavedSize);
                }
                storeSplitterSettings(dockLayoutPanel.isSplitType(), splitterSize, true);

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
                storeSplitterSettings(event.isType(), size, event.isArrowsPress() ? false : true);

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

    private class SplitterSettingsTimeoutTimer extends Timer {
        private Long orientation;
        private Long size;
        private Boolean customDrag = false;

        private SplitterSettingsTimeoutTimer(boolean vertical, int size, Boolean customDrag) {
            setParameters(vertical, size, customDrag);
        }

        public void setParameters(boolean vertical, int size, Boolean customDrag) {
            this.orientation = vertical ? Long.valueOf(1) : Long.valueOf(0);
            this.size = Long.valueOf(size);
            this.customDrag = customDrag;
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
        actionContext.setCustomDrag(customDrag);
        actionContext.setActionConfig(actionConfig);
        final Action action = ComponentRegistry.instance.get(SplitterSettingsActionContext.COMPONENT_NAME);
        action.setInitialContext(actionContext);
        action.perform();
    }
}

    private void storeSplitterSettings(final boolean vertical, final int size, final Boolean customDrag) {
        if (timeoutTimer == null) {
            timeoutTimer = new SplitterSettingsTimeoutTimer(vertical, size, customDrag);
        } else {
            timeoutTimer.cancel();
            timeoutTimer.setParameters(vertical, size, customDrag);
        }
        timeoutTimer.schedule(SCHEDULE_TIMEOUT);
    }
}
