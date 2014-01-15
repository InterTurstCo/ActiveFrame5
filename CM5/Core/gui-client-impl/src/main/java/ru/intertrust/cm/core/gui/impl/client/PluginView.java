package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

//import ru.intertrust.cm.core.config.gui.ActionConfig;

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
    private Widget viewWidget;

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected PluginView(Plugin plugin) {
        this.plugin = plugin;

    }

    public void setVisibleToolbar(final boolean visible) {
        if (!visible && actionToolBar != null) {
            actionToolBar.removeFromParent();
            actionToolBar = null;
        } else if (visible && actionToolBar == null) {
            actionToolBar = new AbsolutePanel();
            actionToolBar.setStyleName("action-bar");


        }
    }

    /**
     * Строит "Панель действий" плагина
     *
     * @return возвращает виджет, отображающий "Панель действий"
     */
    protected void initializeActionToolBar() {
        actionToolBar = new AbsolutePanel();
        actionToolBar.setStyleName("action-bar");
    }

    protected void updateActionToolBar() {
        AbsolutePanel leftSide = new AbsolutePanel();
        leftSide.setStyleName("decorated-action-link");

        if (!(plugin instanceof IsActive)) {
            return;
        }

        actionToolBar.clear();
        ActivePluginData initialData = plugin.getInitialData();
        if (initialData == null) {
            return;
        }
        List<ActionContext> actionContexts = initialData.getActionContexts();
        if (actionContexts == null) {
            return;
        }
        for (final ActionContext actionContext : actionContexts) {
            leftSide.add(ComponentHelper.createToolbarBtn(actionContext, plugin, true));
        }
        actionToolBar.add(leftSide);
        final FlowPanel rightSide = new FlowPanel();
        rightSide.setStyleName("action-bar-right-side");
        rightSide.getElement().getStyle().setFloat(Style.Float.RIGHT);
        actionContexts = getDefaultSystemContexts();
        for (ActionContext context : actionContexts) {
            rightSide.add(ComponentHelper.createToolbarBtn(context, plugin, false));
        }
        actionToolBar.add(rightSide);
    }

    /**
     * Строит и возвращает представление (внешнее отображение) плагина
     *
     * @return виджет, представляющий плагин
     */
    protected abstract IsWidget getViewWidget();

    @Override
    public Widget asWidget() {
        if (viewWidget != null) {
            return viewWidget;
        }

        VerticalPanel panel = new VerticalPanel();
        if (plugin instanceof IsActive) {
            initializeActionToolBar();
            updateActionToolBar();
            if (plugin.displayActionToolBar() && actionToolBar.getWidgetCount() > 0) {
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

    private List<ActionContext> getDefaultSystemContexts() {
        final List<ActionContext> contexts = new ArrayList<ActionContext>();
        contexts.add(new ActionContext(
                createActionConfig("formsize.toggle.action", "toggle form", "icons/form-fullsize.png")));
        contexts.add(new ActionContext(
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
}
