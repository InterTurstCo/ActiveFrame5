package ru.intertrust.cm.core.gui.model.plugin;

/**
 * @author Sergey.Okolot
 */
public class DomainObjectSurferPluginState extends PluginState {
    private boolean toggleEdit;

    public boolean isToggleEdit() {
        return toggleEdit;
    }

    public void setToggleEdit(boolean toggleEdit) {
        this.toggleEdit = toggleEdit;
    }
}
