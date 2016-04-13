package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.04.2016
 * Time: 12:58
 * To change this template use File | Settings | File and Code Templates.
 */
public class ActionCell extends EditableTextCell {

    public ActionCell(String style, String field) {
        super(style, field);
    }

    @Override
    public void render(Context context, String text, SafeHtmlBuilder sb) {
        super.render(context,text,sb);
    }

    @Override
    protected void addClassName(CollectionRowItem item, SafeHtmlBuilder sb) {
        if (item.getParentId() != null) {
            sb.append(SafeHtmlUtils.fromTrustedString(" class=\"childRow\" \"actionCollectionColumn\""));
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString(" class= \"actionCollectionColumn\""));
        }
    }

    @Override
    public Set<String> getConsumedEvents() {
        HashSet<String> events = new HashSet<String>();
        events.add("click");
        return events;
    }
}
