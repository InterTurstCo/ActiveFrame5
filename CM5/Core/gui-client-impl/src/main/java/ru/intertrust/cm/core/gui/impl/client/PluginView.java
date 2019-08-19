package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Node;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionDisplayType;
import ru.intertrust.cm.core.config.gui.action.ActionGroupConfig;
import ru.intertrust.cm.core.config.gui.action.ActionSeparatorConfig;
import ru.intertrust.cm.core.config.gui.action.BaseAttributeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RuleTypeConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SubscribedTypeConfig;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEvent;
import ru.intertrust.cm.core.gui.api.client.event.WidgetBroadcastEventHandler;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.action.ToggleAction;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.calendar.CalendarPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.configurationdeployer.ConfigurationDeployerPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin.HierarchySurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.plugin.PluginManager;
import ru.intertrust.cm.core.gui.impl.client.plugins.report.ReportPlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.reportupload.ReportUploadPlugin;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionException;
import ru.intertrust.cm.core.gui.impl.client.rules.ExpressionHelper;
import ru.intertrust.cm.core.gui.impl.client.util.ActionContextComparator;
import ru.intertrust.cm.core.gui.impl.client.util.LinkUtil;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.action.infobar.InfoBarItem;
import ru.intertrust.cm.core.gui.model.action.infobar.InformationBarContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  private AbsolutePanel infoBar;
  private VerticalPanel viewWidget;
  private MenuBarExt leftMenuBar;

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

  public void setVisibleInfoBar(final boolean visible) {
    if (visible != (infoBar.getParent() != null)) {
      if (visible) {
        updateInfoBar();
        asWidget().insert(infoBar, 1);
      } else {
        infoBar.removeFromParent();
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

  public void updateInfoBar() {
    if (infoBar == null || !plugin.isDisplayInfobar()) {
      return;
    }

    infoBar.clear();
    final ActivePluginData initialData = plugin.getInitialData();

    InformationBarContext context = initialData.getInfoBarContext();
    if (context == null) {
      context = new InformationBarContext();
    }

    for (InfoBarItem infoElement : context.getInfoBarItems()) {
      HorizontalPanel rowBox = new HorizontalPanel();
      Label nameLabel = new Label();
      nameLabel.setText(infoElement.getName());
      nameLabel.getElement().addClassName("labelWidgetDefault");

      Label valueLabel = new Label();
      valueLabel.setText(infoElement.getValue());
      valueLabel.getElement().addClassName("textBoxNonEditable");

      rowBox.add(nameLabel);
      rowBox.add(valueLabel);
      infoBar.add(rowBox);
    }
  }

  public void updateActionToolBar() {
    if (!(plugin instanceof IsActive) || actionToolBar == null || !plugin.displayActionToolBar()) {
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
    leftMenuBar = new MenuBarExt();
    leftMenuBar.setStyleName("decorated-action-link");
    for (ActionContext context : toolbarContext.getContexts(ToolbarContext.FacetName.LEFT)) {
      leftMenuBar.addActionItem(context);
    }
    leftMenuBar.setFocusOnHoverEnabled(false);
    actionToolBar.add(leftMenuBar);
    final MenuBarExt rightMenuBar = new MenuBarExt();
    rightMenuBar.setStyleName("action-bar-right-side");
    for (ActionContext context : rightContexts) {
      rightMenuBar.addActionItem(context);
    }
    rightMenuBar.setFocusOnHoverEnabled(false);
    actionToolBar.add(rightMenuBar);
    addHanler(this);
  }

  public static native void addHanler(PluginView pObject)
    /*-{
        function doc_keyDown(e) {
            if (e.altKey && e.keyCode == 83) {
                e.preventDefault();
                e.stopPropagation();
                pObject.@ru.intertrust.cm.core.gui.impl.client.PluginView::doSaveAction()();
                e.cancelBubble(e);
            }
        };

        $wnd.addEventListener('keydown', doc_keyDown, true);
    }-*/;

  private void doSaveAction() {
    if (leftMenuBar.availableItems.containsKey("aSave") && plugin.isDirty()) {
      leftMenuBar.availableItems.get("aSave").getScheduledCommand().execute();
    }
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

    infoBar = createInfoBar();
    if (plugin.isDisplayInfobar() && (plugin instanceof IsActive)) {
      updateInfoBar();
      if (infoBar.getWidgetCount() > 0) {
        panel.add(infoBar);
      }
    }

    panel.add(getViewWidget());
    viewWidget = panel;
    addExtraStyleClassIfRequired();
    return viewWidget;
  }

  private void addExtraStyleClassIfRequired() {
    if (plugin instanceof DomainObjectSurferPlugin || plugin instanceof ConfigurationDeployerPlugin
        || plugin instanceof CalendarPlugin || plugin instanceof ReportPlugin || plugin instanceof ReportUploadPlugin || plugin instanceof PluginManager || plugin instanceof HierarchySurferPlugin) {
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

  private AbsolutePanel createInfoBar() {
    final AbsolutePanel infobar = new AbsolutePanel();
    return infobar;
  }

  private List<ActionContext> getDefaultSystemContexts() {
    final List<ActionContext> contexts = new ArrayList<ActionContext>();
    final ToggleActionContext fstCtx = new ToggleActionContext(createActionConfigWithImageInDiv(
        "size.toggle.action", LocalizeUtil.get(SIZE_ACTION_TOOLTIP_KEY, SIZE_ACTION_TOOLTIP), ToggleAction.FORM_FULL_SIZE_ACTION_STYLE_NAME, 1000));
    fstCtx.setPushed(Application.getInstance().getCompactModeState().isExpanded());
    contexts.add(fstCtx);
    if (Application.getInstance().getCompactModeState().isRightPanelConfigured()) {
      ToggleActionContext favoritesToggleActionContext = new ToggleActionContext(createActionConfigWithImageInDiv(
          "favorite.toggle.action", LocalizeUtil.get(FAVORITE_ACTION_TOOLTIP_KEY, FAVORITE_ACTION_TOOLTIP),
          ToggleAction.FAVORITE_PANEL_ACTION_STYLE_NAME, 1001));
      favoritesToggleActionContext.setPushed(Application.getInstance().getCompactModeState().isRightPanelExpanded());
      contexts.add(favoritesToggleActionContext);
    }
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

    public Map<String, ActiveMenuItem> availableItems = new HashMap<>();

    public void addActionItem(final ActionContext context) {
      final AbstractActionConfig config = context.getActionConfig();
      if (config instanceof ActionSeparatorConfig) {
        final MenuItemSeparator separator = addSeparator();
        updateByConfig(separator, config);
      } else if (config instanceof ActionConfig) {
        final ScheduleCommandImpl commandImpl = new ScheduleCommandImpl(context);
        final ActiveMenuItem menuItem = new ActiveMenuItem(ComponentHelper.createActionHtmlItem(context), commandImpl);
        commandImpl.setParent(menuItem);
        updateByConfig(menuItem, config);
        menuItem.setTitle(((ActionConfig) config).getTooltip());

        if (((ActionConfig) config).getJsid() != null) {
          menuItem.getElement().setId(((ActionConfig) config).getJsid());
        }
        menuItem.setItemConfig((ActionConfig) config);

        addItem(menuItem);
        availableItems.put(((ActionConfig) config).getName(), menuItem);
      } else if (config instanceof ActionGroupConfig) {
        ActiveMenuItem menuItem = addSubmenu(context, false);
        if (menuItem != null)
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

    private ActiveMenuItem addSubmenu(ActionContext subContext, Boolean isInner) {
      ActionGroupConfig config = subContext.getActionConfig();
      if (!config.isDisplayEmptyGroups() && subContext.getInnerContexts().size() == 0)
        return null;
      else {
        MenuBar mBar = new MenuBar(true);
        mBar.setStyleName("decorated-action-nested-link");
        if (isInner) {
          mBar.addStyleName("decoratedActionLinkPopupInset");
        }

        Collections.sort(subContext.getInnerContexts(), new ActionContextComparator());
        for (ActionContext innerContext : subContext.getInnerContexts()) {
          AbstractActionConfig innerConfig = innerContext.getActionConfig();
          if (innerConfig instanceof ActionSeparatorConfig) {
            mBar.addSeparator();
          } else if (innerConfig instanceof ActionConfig) {
            final ScheduleCommandImpl commandImpl = new ScheduleCommandImpl(innerContext);
            final ActiveMenuItem menuItem = new ActiveMenuItem(ComponentHelper.createActionHtmlItem(innerContext), commandImpl);
            commandImpl.setParent(menuItem);
            updateByConfig(menuItem, config);
            menuItem.setTitle(((ActionConfig) innerConfig).getTooltip());
            mBar.addItem(menuItem);
          } else if (config instanceof ActionGroupConfig) {
            ActiveMenuItem innerItem = addSubmenu(innerContext, true);
            if (innerItem != null) {
              mBar.addItem(innerItem);
            }
          }
        }

        ActiveMenuItem menuItem = new ActiveMenuItem(ComponentHelper.createActionGroupHtmlItem(subContext), mBar);
        updateByConfig(menuItem, config);
        menuItem.setTitle(config.getTooltip());
        return menuItem;
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

  /**
   * @author Ravil
   *         Класс предназначен для расширения стандартного меню GWT возможностью слушать
   *         широковещательные ивенты от элементов управления.
   */
  private class ActiveMenuItem extends MenuItem implements WidgetBroadcastEventHandler {

    private EventBus applicationEventBus = Application.getInstance().getEventBus();
    private ActionConfig itemConfig;

    public ActiveMenuItem(SafeHtml html) {
      super(html);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    public ActiveMenuItem(SafeHtml html, Scheduler.ScheduledCommand cmd) {
      super(html, cmd);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    public ActiveMenuItem(SafeHtml html, MenuBar subMenu) {
      super(html, subMenu);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    public ActiveMenuItem(String text, boolean asHTML, Scheduler.ScheduledCommand cmd) {
      super(text, asHTML, cmd);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    public ActiveMenuItem(String text, boolean asHTML, MenuBar subMenu) {
      super(text, asHTML, subMenu);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    public ActiveMenuItem(String text, Scheduler.ScheduledCommand cmd) {
      super(text, cmd);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    public ActiveMenuItem(String text, MenuBar subMenu) {
      super(text, subMenu);
      applicationEventBus.addHandler(WidgetBroadcastEvent.TYPE, this);
    }

    /**
     * Если элемент подписан на событие от данного публикатора,
     * выполняем действие с ним
     *
     * @param e
     */
    @Override
    public void onEventReceived(WidgetBroadcastEvent e) {
      if ((getActionToolBar().hashCode()==e.getInitiatorToolBarHashCode()
          && subscribedOn(e.getWidgetId())) || (itemConfig.getEventsTypeConfig() != null && e.getBroadcast())) {
        try {
          if (itemConfig.getRulesTypeConfig().getHideRulesTypeConfig() != null) {
            // Если правил сокрытия несколько то их суммарный результат должен быть true
            Boolean shouldByHidden = true;
            for (RuleTypeConfig rule : itemConfig.getRulesTypeConfig().getHideRulesTypeConfig().getRuleTypeConfigs()) {
              shouldByHidden = shouldByHidden & ExpressionHelper.applyExpression(rule.getApplyExpression(), (WidgetsContainer) e.getEventPayload());
            }
            this.setVisible(!shouldByHidden);
          }
        } catch (ExpressionException ex){
          Window.alert(ex.getMessage());
        }
      }
    }

    public void setItemConfig(ActionConfig itemConfig) {
      this.itemConfig = itemConfig;
    }

    private Boolean subscribedOn(String widgetId) {
      if (itemConfig.getEventsTypeConfig() != null) {
        for(SubscribedTypeConfig config : itemConfig.getEventsTypeConfig().getSubscriberTypeConfig().getSubscribedTypeConfigs()){
          if(config.getToId().equals(widgetId)){
            return true;
          }
        }
      }
      return false;
    }
  }
}
