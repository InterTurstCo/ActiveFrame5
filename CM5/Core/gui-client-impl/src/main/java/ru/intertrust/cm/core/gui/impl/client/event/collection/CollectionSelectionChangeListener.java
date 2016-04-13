package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.Plugin;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 01.04.2016
 * Time: 14:53
 * To change this template use File | Settings | File and Code Templates.
 */
public interface CollectionSelectionChangeListener {
    public void onSelectionChange(Plugin plugin, Widget widget, Id id, boolean selected);
}
