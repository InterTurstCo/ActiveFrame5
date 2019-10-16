package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author akarandey
 *         Created on 26.09.2013.
 */
public class RootNodeButton extends HTML {
    private static int VISIBLE_CHARS_LENGTH = 15;
    private static final String SELECTED_STYLE = "selected";
    private static final String NON_SELECTED_STYLE = "non-selected";

    private String name;
    private String image;
    private String displayText;
    private boolean selected;
    private Boolean autoCut;
    private boolean baseAutoCut;
    /**
     * todo will be used action config to initialize instance.
     *
     * @param collectionCount
     * @param name
     * @param image
     * @param displayText
     */
    @Deprecated //use RootNodeButton(LinkConfig linkConfig) instead
    public RootNodeButton(final Long collectionCount, final String name, final String image, final String displayText) {
        super(getHtmlTemplate(image, displayText, collectionCount, true, true));
        this.name = name;
        this.image = image;
        this.displayText = displayText;
        setStyleName(NON_SELECTED_STYLE);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        if (displayText != null && displayText.length() >= VISIBLE_CHARS_LENGTH) {
            setTitle(displayText);
        }
    }
    public RootNodeButton(LinkConfig linkConfig, boolean baseAutoCut){
        super(getHtmlTemplate(linkConfig, baseAutoCut));
        this.name = linkConfig.getName();
        this.image = linkConfig.getImage();
        this.displayText = linkConfig.getDisplayText();
        this.autoCut = linkConfig.isAutoCut();
        this.baseAutoCut = baseAutoCut;
        setStyleName(NON_SELECTED_STYLE);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
        if(linkConfig.getTooltip() != null){
            setTitle(linkConfig.getTooltip());
        } else if (displayText != null && displayText.length() >= VISIBLE_CHARS_LENGTH){
            setTitle(displayText);
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
        super.setHTML(getHtmlTemplate(image, displayText, collectionCount, autoCut, baseAutoCut));
    }

    private static String getHtmlTemplate(LinkConfig linkConfig, boolean baseAutoCut){
        return getHtmlTemplate(linkConfig.getImage(), linkConfig.getDisplayText(), null, linkConfig.isAutoCut(), baseAutoCut);
    }

    private static String getHtmlTemplate(final String image, final String displayText, final Long collectionCount, Boolean linkAutoCut,
                                  boolean baseAutoCut) {
        boolean autoCut = (linkAutoCut != null && linkAutoCut) || (linkAutoCut == null && baseAutoCut);
        String styleForWordsAutoCut = autoCut ? " style = \"overflow: hidden; text-overflow: ellipsis; white-space: nowrap;\"" : "";
        final StringBuilder builder = new StringBuilder("<li><a><img width='60' height='50' border='0' alt='' src='")
                .append(GlobalThemesManager.getResourceFolder()).append(image)
                .append("'><span ")
                .append(styleForWordsAutoCut)
                .append(">")
                .append(displayText)
                .append("</span>");
        if (collectionCount != null) {
            builder.append("<small>").append(collectionCount).append("</small>");
        }
        builder.append("</a></li>");
        return builder.toString();
    }
}
