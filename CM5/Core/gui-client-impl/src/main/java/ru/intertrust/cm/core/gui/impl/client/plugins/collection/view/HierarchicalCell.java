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
public class HierarchicalCell extends AbstractTextCell {
    private String drillDownStyle;

    public HierarchicalCell(String style, String drillDownStyle, String field) {
        super(style, field);
        this.drillDownStyle = drillDownStyle;
    }

    @Override
    public void render(Context context, String text, SafeHtmlBuilder sb) {
        if ("combined-link".equals(drillDownStyle)) {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchical-column\" " + style + "/><span class=\"expand-arrow\">"));
            sb.append(SafeHtmlUtils.fromString(text));
            sb.append(SafeHtmlUtils.fromTrustedString(" ►<span></div>"));
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString("<div class=\"hierarchical-column\" " + style + "/>"));
            sb.append(SafeHtmlUtils.fromString(text));
            sb.append(SafeHtmlUtils.fromTrustedString(" <span class=\"expand-arrow\">►<span></div>"));
        }
    }

    @Override
    public Set<String> getConsumedEvents() {
        HashSet<String> events = new HashSet<String>();
        events.add("click");
        return events;
    }
}