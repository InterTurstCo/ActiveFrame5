package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author akarandey
 *         Created on 26.09.2013.
 */
public class RootNodeButton extends HTML {
    private static final String SELECTED_STYLE = "selected";
    private static final String NON_SELECTED_STYLE = "non-selected";

    private final String name;
    private final String image;
    private final String displayText;
    private boolean selected;

    /**
     * todo will be used action config to initialize instance.
     * @param collectionCount
     * @param name
     * @param image
     * @param displayText
     */
    public RootNodeButton(final Long collectionCount, final String name, final String image, final String displayText) {
        super(getHtml(image, displayText, collectionCount));
        this.name = name;
        this.image = image;
        this.displayText = displayText;
        setStyleName(NON_SELECTED_STYLE);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        if (name != null) {
            setTitle(name);
        }
    }

    public String getName() {
        return name;
    }

    public void setSelected(final boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            setStyleName(selected ? SELECTED_STYLE : NON_SELECTED_STYLE);
        }
    }

    public void updateCollectionCount(Long collectionCount) {
        super.setHTML(getHtml(image, displayText, collectionCount));
    }

    private static String getHtml(final String image, final String displayText, final Long collectionCount) {
        final StringBuilder builder =
                new StringBuilder("<li><a><img width='60' height='50' border='0' alt='' src='")
                        .append(image).append("'><span>").append(displayText).append("</span>");
        if (collectionCount != null) {
            builder.append("<small>").append(collectionCount).append("</small>");
        }
        builder.append("</a></li>");
        return builder.toString();
    }
}
