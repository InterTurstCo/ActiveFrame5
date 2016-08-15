package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 12:20
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class HierarchyNode extends Composite  {

    protected EventBus localBus;
    protected AbsolutePanel rootPanel;
    protected HorizontalPanel headerPanel;
    protected VerticalPanel childPanel;
    protected HierarchyGuiElementsFactory guiElementsFactory;
    protected HierarchyGuiFactory guiFactory;
    protected Boolean expanded = false;



    protected abstract void addRepresentationCells(Panel container);


}
