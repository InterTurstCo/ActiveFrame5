package ru.intertrust.cm.core.gui.model.plugin;

/**
 * @author Sergey.Okolot
 */
public class FormPluginState implements PluginState {

    private boolean editable;
    private boolean toggleEdit;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isToggleEdit() {
        return toggleEdit;
    }

    public void setToggleEdit(boolean toggleEdit) {
        this.toggleEdit = toggleEdit;
    }

    @Override
    public FormPluginState createClone() {
        final FormPluginState clone = new FormPluginState();
        clone.editable = this.editable;
        clone.toggleEdit = this.toggleEdit;
        return clone;
    }
}
