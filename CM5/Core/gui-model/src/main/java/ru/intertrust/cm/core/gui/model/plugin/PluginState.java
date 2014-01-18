package ru.intertrust.cm.core.gui.model.plugin;

import java.io.Serializable;

/**
 * Defines marker of state of plugin (full screen mode, editable etc).
 *
 * @author Sergey.Okolot
 */
public class PluginState implements Serializable, Cloneable {

    private boolean fullScreen;

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public PluginState createClone() {
        final PluginState clone = new PluginState();
        clone.fullScreen = this.fullScreen;
        return clone;
    }

    protected void fillCloneSuperData(final PluginState clone) {
        clone.fullScreen = this.fullScreen;
    }
}
