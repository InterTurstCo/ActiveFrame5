package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

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
        AbsolutePanel decoratedActionLink = new AbsolutePanel();
        decoratedActionLink.setStyleName("decorated-action-link");

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
            final ActionConfig actionConfig = actionContext.getActionConfig();
            Hyperlink hyperlink = new Hyperlink(actionConfig.getText(), actionConfig.getText());
            if (actionConfig.getImageUrl() != null) {
                Image actionPic = new Image(actionConfig.getImageUrl());
                decoratedActionLink.add(actionPic);
                actionPic.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        String component = actionConfig.getComponent();
                        if (component == null) {
                            component = "generic.workflow.action";
                        }
                        Action action = ComponentRegistry.instance.get(component);
                        action.setInitialContext(actionContext);
                        action.setPlugin(plugin);
                        action.execute();
                    }
                });
            }
            hyperlink.setStyleName("action-bar-button");
            //actionPic.setStyleName("action-bar-button");
            decoratedActionLink.add(hyperlink);

            actionToolBar.add(decoratedActionLink);


            hyperlink.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String component = actionConfig.getComponent();
                    if (component == null) {
                        component = "generic.workflow.action";
                    }
                    Action action = ComponentRegistry.instance.get(component);
                    action.setInitialContext(actionContext);
                    action.setPlugin(plugin);
                    action.execute();
                }
            });
        }

        final FocusPanel rightButton = new FocusPanel();
        rightButton.setStyleName("action-bar-right-button-non-active");
        actionToolBar.add(rightButton);
        rightButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(rightButton.getStyleName().equals("action-bar-right-button-non-active")){
                    rightButton.addStyleName("action-bar-right-button-active");
                }
                else{
                    rightButton.removeStyleName("action-bar-right-button-active");
                }
            }
        });

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

}
