package ru.intertrust.cm.core.gui.model.plugin;

/**
 * @author Sergey.Okolot
 */
public class FormPluginState implements PluginState {

    private boolean editable = true;
    private boolean toggleEdit;
    private boolean inCentralPanel;
    private DomainObjectSource domainObjectSource = DomainObjectSource.COLLECTION;

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

    public boolean isInCentralPanel() {
        return inCentralPanel;
    }

    public void setInCentralPanel(boolean inCentralPanel) {
        this.inCentralPanel = inCentralPanel;
    }

    public DomainObjectSource getDomainObjectSource() {
        return domainObjectSource;
    }

    public void setDomainObjectSource(DomainObjectSource domainObjectSource) {
        this.domainObjectSource = domainObjectSource;
    }


    @Override
    public FormPluginState createClone() {
        final FormPluginState clone = new FormPluginState();
        clone.editable = this.editable;
        clone.toggleEdit = this.toggleEdit;
        clone.inCentralPanel = this.inCentralPanel;
        clone.domainObjectSource = this.domainObjectSource;
        return clone;
    }
}
