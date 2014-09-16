package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lesia Puhova
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class HierarchyCell extends TextCell {

    public HierarchyCell(String style) {
        super(style);
    }

    @Override
    public void render(Context context, String text, SafeHtmlBuilder sb) {
        sb.append(SafeHtmlUtils.fromTrustedString("<a " + style + "/>" + text + " ►</a>"));
    }

    @Override
    public Set<String> getConsumedEvents() {
        HashSet<String> events = new HashSet<String>();
        events.add("click");
        return events;
    }
}