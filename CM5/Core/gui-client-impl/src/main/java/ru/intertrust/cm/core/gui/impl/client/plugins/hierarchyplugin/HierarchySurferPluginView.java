package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchySurferConfig;
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
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchySurferPluginData;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.08.2016
 * Time: 10:18
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchySurferPluginView extends PluginView {

    private static final int SCHEDULE_TIMEOUT = 3000;
    private int surferWidth;
    private int surferHeight;
    private int horizontalSplitterSavedSize = -1;
    private int verticalSplitterSavedSize = -1;
    private FlowPanel southRootWidget = new FlowPanel();
    private SimplePanel northRootWidget = new SimplePanel();
    private AbsolutePanel rootSurferPanel;
    private HierarchySurferPlugin hierarchySurferPlugin;
    private SplitterEx dockLayoutPanel;
    private static Logger log = Logger.getLogger("HierarchySurfer");
    private EventBus eventBus;
    private SplitterSettingsTimeoutTimer timeoutTimer;

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected HierarchySurferPluginView(Plugin plugin) {
        super(plugin);
        hierarchySurferPlugin = (HierarchySurferPlugin)plugin;
        surferWidth = plugin.getOwner().getVisibleWidth();
        surferHeight = plugin.getOwner().getVisibleHeight();
        initSplitter();
        southRootWidget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        eventBus = hierarchySurferPlugin.getLocalEventBus();
        addSplitterWidgetResizeHandler();
    }

    private void initSplitter() {
        dockLayoutPanel = new SplitterEx(8, hierarchySurferPlugin.getLocalEventBus()) {
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
        eventBus.addHandler(SplitterWidgetResizerEvent.EVENT_TYPE, new SplitterWidgetResizerEventHandler() {
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

    @Override
    public IsWidget getViewWidget() {
        rootSurferPanel = new AbsolutePanel();
        rootSurferPanel.setStyleName("centerTopBottomDividerRoot");
        northRootWidget.addStyleName("topPanel");
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("centerTopBottomDividerRootInnerDiv");
        rootSurferPanel.add(container);
        container.add(dockLayoutPanel);
        HierarchySurferPluginData pluginData = plugin.getInitialData();
        final boolean isVertical = Integer.valueOf(1).equals(pluginData.getSplitterOrientation());
        //final Integer splitterSize = pluginData.getSplitterPosition();
        final Integer splitterSize = 459;
        if (isVertical) {
            dockLayoutPanel.addWest(northRootWidget, splitterSize);
        } else {
            dockLayoutPanel.addNorth(northRootWidget, splitterSize - dockLayoutPanel.getSplitterSize());
        }
        splitterSetSize();
        dockLayoutPanel.add(southRootWidget);
        if (splitterSize != null) {
            dockLayoutPanel.setCustomSize(splitterSize);
        }
        final HierarchySurferConfig config  = (HierarchySurferConfig)hierarchySurferPlugin.getConfig();
        if (config != null) {
            PluginPanel hierarchyPluginPanel = new PluginPanel();
            hierarchyPluginPanel.setVisibleWidth(surferWidth);
            hierarchyPluginPanel.setVisibleHeight(surferHeight / 2);
            hierarchyPluginPanel.open(hierarchySurferPlugin.getHierarchyPlugin());

            final PluginPanel formPluginPanel = new PluginPanel();
            formPluginPanel.setVisibleWidth(surferWidth);
            formPluginPanel.setVisibleHeight(surferHeight / 2);
            formPluginPanel.open(hierarchySurferPlugin.getFormPlugin());
            formPluginPanel.asWidget().addStyleName("formContainer");
            northRootWidget.add(hierarchyPluginPanel);
            southRootWidget.add(formPluginPanel);
        }
        Application.getInstance().unlockScreen();
        Application.getInstance().getHistoryManager()
                .setMode(HistoryManager.Mode.APPLY, plugin.getClass().getSimpleName());
        return rootSurferPanel;
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
