package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CurrentRowChangeListener;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.04.2016
 * Time: 15:54
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("table.viewer.row.selected")
public class RowChangedAction extends BaseComponent implements CurrentRowChangeListener {

    @Override
    public void onRowChange(Plugin plugin, Widget widget, Id id, boolean isUserAction) {
        Window.alert("Action "+id);
    }

    @Override
    public Component createNew() {
        return new RowChangedAction();
    }
}
