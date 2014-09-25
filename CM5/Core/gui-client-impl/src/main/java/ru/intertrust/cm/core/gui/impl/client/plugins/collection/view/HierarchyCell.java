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

    private String drillDownStyle;

    public HierarchyCell(String style, String drillDownStyle) {
        super(style);
        this.drillDownStyle = drillDownStyle;
    }

    @Override
    public void render(Context context, String text, SafeHtmlBuilder sb) {
        if ("combined-link".equals(drillDownStyle)) {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchical-column\" " + style + "/><span class=\"expand-arrow\">"
                    + text + " ►<span></div>"));
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchical-column\" " + style + "/>" + text + " <span class=\"expand-arrow\">►<span></div>"));
        }
    }

    @Override
    public Set<String> getConsumedEvents() {
        HashSet<String> events = new HashSet<String>();
        events.add("click");
        return events;
    }
}