package ru.intertrust.cm.core.gui.impl.action;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ListCellRenderFactory;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;


/**
 * Created by Ravil on 19.05.2017.
 */
@ComponentName("render.factory")
public class TestCellRenderer implements ListCellRenderFactory,Component {
    @Override
    public AbstractCell getCellRendererInstance(String cellType) {
        if("ItemRowType".equals(cellType)){
            return new RowItemCell();
        }
        return null;
    }

    @Override
    public String getName() {
        return "render.factory";
    }

    @Override
    public Component createNew() {
        return new TestCellRenderer();
    }

    private class RowItemCell extends AbstractCell<CollectionRowItem> {
        @Override
        public void render(Context context, CollectionRowItem value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.appendHtmlConstant("<div style=\"size:200%;font-weight:bold;border-bottom: 1px solid grey;\">");
                sb.appendEscaped(value.getId().toString());
                sb.appendHtmlConstant("</div>");
            }
        }
    }
}
