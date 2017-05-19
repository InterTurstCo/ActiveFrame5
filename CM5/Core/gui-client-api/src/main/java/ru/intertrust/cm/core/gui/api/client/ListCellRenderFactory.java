package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.cell.client.AbstractCell;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.List;

/**
 * Created by Ravil on 18.05.2017.
 */
public interface ListCellRenderFactory {
    AbstractCell  getCellRendererInstance(String cellType);
}
