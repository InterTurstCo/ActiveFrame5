package ru.intertrust.cm.core.gui.model.plugin;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

/**
 *
 * @author Sergey.Okolot
 */
public class FormPluginMode implements Serializable {

    public static enum Mode {

    };

    private Collection<FormMode> mode;

    public FormPluginMode() {
        mode = new HashSet<FormMode>();
    }

    public void clear() {
        mode.clear();
    }

    public boolean hasMode(final FormMode mode) {
        return this.mode.contains(mode);
    }

    public void updateMode(final FormMode mode, final boolean insert) {
        if (insert) {
            this.mode.add(mode);
        } else {
            this.mode.remove(mode);
        }
    }

    public void updateMode(final EnumSet<FormMode> mode, final boolean insert) {
        if (insert) {
            this.mode.addAll(mode);
        } else {
            this.mode.removeAll(mode);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("FormPluginMode: ").append(mode).toString();
    }
}
