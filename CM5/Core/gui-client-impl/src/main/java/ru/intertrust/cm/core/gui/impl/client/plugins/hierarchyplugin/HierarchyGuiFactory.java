package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:22
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGuiFactory {

    public Widget buildGroup(HierarchyGroupConfig aGroupConfig){
        return new HierarchyGroupView(aGroupConfig);
    }


}
