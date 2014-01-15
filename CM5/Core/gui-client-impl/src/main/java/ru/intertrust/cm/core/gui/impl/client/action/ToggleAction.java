package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Sergey.Okolot
 */
public abstract class ToggleAction extends Action {

    public static final String OFF_SUFFIX = "-off.png";
    public static final String ON_SUFFIX = "-on.png";
    protected static final String IMAGE_SUFFIX = ".png";

    private Image image;
    private Anchor anchor;

    protected Image getImage() {
        return image;
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    protected Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(final Anchor anchor) {
        this.anchor = anchor;
    }
}
