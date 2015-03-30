package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.config.gui.action.*;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.impl.client.plugins.calendar.CalendarPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.configurationdeployer.ConfigurationDeployerPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.FAVORITE_ACTION_TOOLTIP_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.SIZE_ACTION_TOOLTIP_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.FAVORITE_ACTION_TOOLTIP;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.SIZE_ACTION_TOOLTIP;

/**
 * Базовый класс представления плагина.
 *
 * @author Denis Mitavskiy
 *         Date: 19.08.13
 *         Time: 13:57
 */
public abstract class PluginView implements IsWidget {

    protected Plugin plugin;
    protected static Logger log = Logger.getLogger("PluginView console logger");

    private AbsolutePanel actionToolBar;
    private VerticalPanel viewWidget;

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected PluginView(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setVisibleToolbar(final boolean visible) {
        if (visible != (actionToolBar.getParent() != null)) {
            if (visible) {
                updateActionToolBar();
                asWidget().insert(actionToolBar, 0);
            } else {
                actionToolBar.removeFromParent();
            }
        }
    }

    protected Panel createBreadCrumbsPanel() {
        NavigationConfig navigationConfig = plugin.getNavigationConfig();
        if (navigationConfig == null) {
            return null;
        }
        String link = Application.getInstance().getHistoryManager().getLink();
        Panel breadCrumbPanel = buildBreadCrumbPanel(link, navigationConfig);
        breadCrumbPanel.addStyleName("breadcrumbPanel");
        return breadCrumbPanel;
    }

    private Panel buildBreadCrumbPanel(String link, NavigationConfig navigationConfig) {
        LinkUtil.markNavigationHierarchy(navigationConfig.getLinkConfigList(), null, null);
        AbsolutePanel breadCrumbComponents = new AbsolutePanel();
        List<LinkConfig> foundLinks = new ArrayList<>();
        LinkUtil.findLink(link, navigationConfig.getLinkConfigList(), foundLinks);
        List<IsWidget> breadcrumbWidgets = new ArrayList<>();
        if (!foundLinks.isEmpty()) {
            LinkConfig currentLinkConfig = foundLinks.get(0);
            while (true) {
                breadcrumbWidgets.add(new Hyperlink(currentLinkConfig.getDisplayText(), "link=" + currentLinkConfig.getName()));
                ChildLinksConfig parentChildLinksConfig = currentLinkConfig.getParentChildLinksConfig();
                if (parentChildLinksConfig != null && parentChildLinksConfig.getGroupName() != null) {
                    breadcrumbWidgets.add(new Label(currentLinkConfig.getParentChildLinksConfig().getGroupName()));
                }
                currentLinkConfig = currentLinkConfig.getParentLinkConfig();
                if (currentLinkConfig == null) {
                    break;
                }
            }
        }
        List<LinkConfig> hierarchicalLinks = plugin.getNavigationConfig().getHierarchicalLinkList();
        LinkConfig currentLinkConfig = null;
        for (LinkConfig hierarchicalLink : hierarchicalLinks) {
            if (hierarchicalLink.getName().equals(link)) {
                currentLinkConfig = hierarchicalLink;
            }
        }
        while (currentLinkConfig != null) {
            breadcrumbWidgets.add(new Hyperlink(currentLinkConfig.getDisplayText(), "link=" + currentLinkConfig.getName()));
            currentLinkConfig = currentLinkConfig.getParentLinkConfig();
        }

        Collections.reverse(breadcrumbWidgets);
        Iterator<IsWidget> iterator = breadcrumbWidgets.iterator();
        while (iterator.hasNext()) {
            IsWidget next = iterator.next();
            breadCrumbComponents.add(next);
            if (iterator.hasNext()) {
                breadCrumbComponents.add(new Label("/"));
            }
        }
        return breadCrumbComponents;
    }

    public void updateActionToolBar() {
        if (!(plugin instanceof IsActive) || actionToolBar == null) {
            return;
        }
        actionToolBar.clear();
        final ActivePluginData initialData = plugin.getInitialData();
        final ToolbarContext toolbarContext;
        if (initialData == null) {
            toolbarContext = new ToolbarContext();
        } else {
            toolbarContext = initialData.getToolbarContext();
        }
        List<ActionContext> rightContexts = new ArrayList<>(toolbarContext.getContexts(ToolbarContext.FacetName.RIGHT));
        rightContexts.addAll(getDefaultSystemContexts());

        toolbarContext.sortActionContexts();
        final MenuBarExt leftMenuBar = new MenuBarExt();
        leftMenuBar.setStyleName("decorated-action-link");
        for (ActionContext context : toolbarContext.getContexts(ToolbarContext.FacetName.LEFT)) {
            leftMenuBar.addActionItem(context);
        }
        actionToolBar.add(leftMenuBar);
        final MenuBarExt rightMenuBar = new MenuBarExt();
        rightMenuBar.setStyleName("action-bar-right-side");
        for (ActionContext context : rightContexts) {
            rightMenuBar.addActionItem(context);
        }
        actionToolBar.add(rightMenuBar);
    }

    /**
     * Строит и возвращает представление (внешнее отображение) плагина
     *
     * @return виджет, представляющий плагин
     */
    public abstract IsWidget getViewWidget();

    @Override
    public VerticalPanel asWidget() {
        if (viewWidget != null) {
            return viewWidget;
        }
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("one-handred-percent-style");
        actionToolBar = createToolbar();
        if (plugin.displayActionToolBar() && (plugin instanceof IsActive)) {
            updateActionToolBar();
            if (actionToolBar.getWidgetCount() > 0) {
                panel.add(actionToolBar);
            }
        }
        panel.add(getViewWidget());
        viewWidget = panel;
        addExtraStyleClassIfRequired();
        return viewWidget;
    }

    private void addExtraStyleClassIfRequired() {
        if (plugin instanceof DomainObjectSurferPlugin || plugin instanceof ConfigurationDeployerPlugin || plugin instanceof CalendarPlugin) {
            Node node = viewWidget.getElement().getFirstChildElement().getLastChild();
            node.getFirstChild().getParentElement().addClassName("pluginExtraClass");
        }
    }

    /**
     * Перерисовует cодержимое плагин панели после изменения размеров панели
     */
    public void onPluginPanelResize() {

    }

    private AbsolutePanel createToolbar() {
        final AbsolutePanel toolbar = new AbsolutePanel();
        toolbar.setStyleName("action-bar");
        return toolbar;
    }

    private List<ActionContext> getDefaultSystemContexts() {
        final List<ActionContext> contexts = new ArrayList<ActionContext>();
        final ToggleActionContext fstCtx = new ToggleActionContext(createActionConfigWithImageInDiv(
                "size.toggle.action", LocalizeUtil.get(SIZE_ACTION_TOOLTIP_KEY, SIZE_ACTION_TOOLTIP), ToggleAction.FORM_FULL_SIZE_ACTION_STYLE_NAME, 1000));
        fstCtx.setPushed(Application.getInstance().getCompactModeState().isExpanded());
        contexts.add(fstCtx);
        contexts.add(new ToggleActionContext(createActionConfigWithImageInDiv(
                "favorite.toggle.action", LocalizeUtil.get(FAVORITE_ACTION_TOOLTIP_KEY, FAVORITE_ACTION_TOOLTIP), ToggleAction.FAVORITE_PANEL_ACTION_STYLE_NAME, 1001)));
        return contexts;
    }

    public AbsolutePanel getActionToolBar() {
        return actionToolBar;
    }

    private ActionConfig createActionConfig(final String componentName, final String shortDesc,
                                            final String imageUrl, final int order) {
        final ActionConfig config = new ActionConfig(componentName, componentName);
        config.setImageUrl(imageUrl);
        config.setTooltip(shortDesc);
        config.setDisplay(ActionDisplayType.toggleButton);
        config.setOrder(order);
        config.setDirtySensitivity(false);
        config.setImmediate(true);
        return config;
    }

    private ActionConfig createActionConfigWithImageInDiv(final String componentName, final String shortDesc,
                                                          final String imageClass, final int order) {
        final ActionConfig config = new ActionConfig(componentName, componentName);
        config.setImageClass(imageClass);
        config.setTooltip(shortDesc);
        config.setDisplay(ActionDisplayType.toggleButton);
        config.setOrder(order);
        config.setDirtySensitivity(false);
        config.setImmediate(true);
        return config;
    }

    private class MenuBarExt extends MenuBar {

        public void addActionItem(final ActionContext context) {
            final AbstractActionConfig config = context.getActionConfig();
            if (config instanceof ActionSeparatorConfig) {
                final MenuItemSeparator separator = addSeparator();
                updateByConfig(separator, config);
            } else if (config instanceof ActionConfig) {
                final ScheduleCommandImpl commandImpl = new ScheduleCommandImpl(context);
                final MenuItem menuItem = new MenuItem(ComponentHelper.createActionHtmlItem(context), commandImpl);
                commandImpl.setParent(menuItem);
                updateByConfig(menuItem, config);
                menuItem.setTitle(((ActionConfig) config).getTooltip());
                addItem(menuItem);
            } else if (config instanceof ActionGroupConfig) {
                //TODO: Проверить если нет вложенных контекстов и атрибут displayEmptyGroup=false то ничего не делать
                Command cmd = new Command() {
                    public void execute() {
                        Window.alert("You selected a menu item!");
                    }
                };
                MenuBar mBar = new MenuBar();
                mBar.addItem("Item 1", cmd).setTitle("Item 1");
                mBar.addItem("Item 2", cmd).setTitle("Item 2");
                mBar.addItem("Item 3", cmd).setTitle("Item 3");
                final MenuItem menuItem = new MenuItem(ComponentHelper.createActionGroupHtmlItem(context), mBar);
                updateByConfig(menuItem, config);
                menuItem.setTitle(((ActionGroupConfig) config).getTooltip());
                addItem(menuItem);
            } else {
                throw new IllegalArgumentException("Not support context " + context);
            }
        }

        private void updateByConfig(UIObject uiobj, BaseAttributeConfig config) {
            if (config.getStyleClass() != null) {
                uiobj.setStyleName(config.getStyleClass());
            }
        }
    }

    private class ScheduleCommandImpl implements Scheduler.ScheduledCommand {
        private UIObject parent;
        private final ActionContext context;

        private ScheduleCommandImpl(final ActionContext context) {
            this.context = context;
        }

        public void setParent(final UIObject parent) {
            this.parent = parent;
        }

        @Override
        public void execute() {
            final ActionConfig config = context.getActionConfig();
            String componentName = config.getComponentName();
            final Action action = ComponentRegistry.instance.get(componentName);
            action.setInitialContext(context);
            action.setPlugin(plugin);
            action.perform();
            if (ActionDisplayType.toggleButton == config.getDisplay()) {
                parent.getElement().setInnerHTML(ComponentHelper.createActionHtmlItem(context).asString());
            }
        }
    }

}
