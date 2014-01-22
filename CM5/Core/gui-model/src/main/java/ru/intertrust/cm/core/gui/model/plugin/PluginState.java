package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Pair;

import java.io.Serializable;

/**
 * Defines marker of state of plugin (full screen mode, editable etc).
 *
 * @author Sergey.Okolot
 */
public class PluginState implements Serializable, Cloneable {

    private boolean fullScreen;
    private int leftWidth;
    private int headerHeight;

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public int getLeftWidth() {
        return leftWidth;
    }

    public void setLeftWidth(int leftWidth) {
        this.leftWidth = leftWidth;
    }

    public int getHeaderHeight() {
        return headerHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
    }

    public PluginState createClone() {
        final PluginState clone = new PluginState();
        fillCloneSuperData(clone);
        return clone;
    }

    protected void fillCloneSuperData(final PluginState clone) {
        clone.fullScreen = this.fullScreen;
        clone.leftWidth = this.leftWidth;
        clone.headerHeight = this.headerHeight;
    }
}
