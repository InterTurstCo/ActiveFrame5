package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToggleActionContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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

    protected void updateActionToolBar() {
        if (!(plugin instanceof IsActive) || actionToolBar == null) {
            return;
        }
        actionToolBar.clear();
        final ActivePluginData initialData = plugin.getInitialData();
        if (initialData == null) {
            return;
        }
        List<ActionContext> actionContexts = initialData.getActionContexts();
        if (actionContexts == null || actionContexts.isEmpty()) {
            return;
        }
        final AbsolutePanel leftSide = new AbsolutePanel();
        leftSide.setStyleName("decorated-action-link");
        for (final ActionContext actionContext : actionContexts) {
            leftSide.add(ComponentHelper.createToolbarBtn(actionContext, plugin, true));
        }
        if (leftSide.getWidgetCount() > 0) {
            actionToolBar.add(leftSide);
        }
        final FlowPanel rightSide = new FlowPanel();
        rightSide.setStyleName("action-bar-right-side");
        rightSide.getElement().getStyle().setFloat(Style.Float.RIGHT);
        actionContexts = getDefaultSystemContexts();
        for (ActionContext context : actionContexts) {
            rightSide.add(ComponentHelper.createToolbarBtn(context, plugin, false));
        }
        if (rightSide.getWidgetCount() > 0) {
            actionToolBar.add(rightSide);
        }
    }

    /**
     * Строит и возвращает представление (внешнее отображение) плагина
     *
     * @return виджет, представляющий плагин
     */
    protected abstract IsWidget getViewWidget();

    @Override
    public VerticalPanel asWidget() {
        if (viewWidget != null) {
            return viewWidget;
        }
        VerticalPanel panel = new VerticalPanel();
        actionToolBar = createToolbar();
        if (plugin.displayActionToolBar() && (plugin instanceof IsActive)) {
            updateActionToolBar();
            if (actionToolBar.getWidgetCount() > 0) {
                panel.add(actionToolBar);
            }
        }
        panel.add(getViewWidget());
        viewWidget = panel;
        return viewWidget;
    }

    /**
     * Перерисовует cодержимое плагин панели после изменения размеров панели
     */
    public  void onPluginPanelResize(){

    }

    private AbsolutePanel createToolbar() {
        final AbsolutePanel toolbar = new AbsolutePanel();
        toolbar.setStyleName("action-bar");
        return toolbar;
    }

    private List<ActionContext> getDefaultSystemContexts() {
        final List<ActionContext> contexts = new ArrayList<ActionContext>();
        final ToggleActionContext fstCtx = new ToggleActionContext(
                createActionConfig("size.toggle.action", "toggle form", "icons/form-fullsize.png"));
        fstCtx.setPushed(Application.getInstance().getCompactModeState().isExpanded());
        contexts.add(fstCtx);
        contexts.add(new ToggleActionContext(
                createActionConfig("favorite.toggle.action", "favorites", "icons/favorite-panel.png")));
        return contexts;
    }

    private ActionConfig createActionConfig(final String componentName, final String shortDesc, final String imageUrl) {
        final ActionConfig config = new ActionConfig(componentName, componentName);
        config.setImageUrl(imageUrl);
        config.setShortDesc(shortDesc);
        config.setToggle(true);
        return config;
    }

    public AbsolutePanel getActionToolBar() {
        return actionToolBar;
    }
}
