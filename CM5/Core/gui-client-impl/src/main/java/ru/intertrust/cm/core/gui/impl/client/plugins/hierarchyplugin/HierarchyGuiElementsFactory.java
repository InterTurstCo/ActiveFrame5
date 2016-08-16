package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.HierarchyActionEvent;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:47
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGuiElementsFactory {

    public Widget buildExpandCell(final EventBus anEventBus, final Id aParentId) {
        final Image image = new Image(GlobalThemesManager.getCurrentTheme().chevronRight());
        image.addClickHandler(new ClickHandler() {
            Boolean ex = false;
            @Override
            public void onClick(ClickEvent event) {

                if (ex) {
                    image.setResource(GlobalThemesManager.getCurrentTheme().chevronRight());
                    anEventBus.fireEvent(new ExpandHierarchyEvent(false,aParentId));
                } else {
                    image.setResource(GlobalThemesManager.getCurrentTheme().chevronDown());
                    anEventBus.fireEvent(new ExpandHierarchyEvent(true,aParentId));
                }
                ex = !ex;
            }
        });
        return image;
    }

    public Widget buildActionButton(final EventBus anEventBus, final HierarchyPluginStaticData.Actions anAction){
        final Image image;
        switch(anAction){
            case GROUPREFRESH:
                image = new Image(GlobalThemesManager.getCurrentTheme().iconRefresh());
                break;
            case GROUPSORT:
                image = new Image(GlobalThemesManager.getCurrentTheme().iconSort());
                break;
            case GROUPADD:
                image = new Image(GlobalThemesManager.getCurrentTheme().iconAdd());
                break;
            default:
                image = new Image(GlobalThemesManager.getCurrentTheme().arrowPlus());
        }

        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                    anEventBus.fireEvent(new HierarchyActionEvent(anAction));
            }
        });
        image.setTitle(anAction.toString());
        return image;
    }

    public Scheduler.ScheduledCommand getACommand(){
        Scheduler.ScheduledCommand menuItemCommand = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
            }
        };
        return menuItemCommand;
    }
}
