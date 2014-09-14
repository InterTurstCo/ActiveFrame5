package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget;

import com.google.web.bindery.event.shared.EventBus;

import java.util.List;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.ASCEND_ARROW;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.DESCEND_ARROW;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 05/03/14
 *         Time: 12:05 PM
 */
public abstract class HeaderWidget {
    protected String html;
    protected String title;
    protected boolean focused;
    protected EventBus eventBus;

    public abstract boolean hasFilter();

    public abstract String getId();

    public abstract String getFilterValuesRepresentation();

    public abstract void setFilterValuesRepresentation(String filterValue);

    public abstract String getFieldName();

    public abstract boolean isShowFilter();

    public abstract void setShowFilter(boolean showFilter);

    public abstract void init();

    public String getHtml() {
        return html;
    }

    public abstract List<String> getFilterValues();
    public abstract void setFilterInputWidth(int filterWidth);

    protected String getTitleHtml() {
        StringBuilder titleBuilder = new StringBuilder("<div  class=\"header-label\"><p>");
        titleBuilder.append(title);
        titleBuilder.append("</p><p style=\"display:none\">");
        titleBuilder.append(title);
        titleBuilder.append(ASCEND_ARROW);
        titleBuilder.append("</p><p style=\"display:none\">");
        titleBuilder.append(title);
        titleBuilder.append(DESCEND_ARROW);
        titleBuilder.append("</p></div>");
        return titleBuilder.toString();
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
