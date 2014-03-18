package ru.intertrust.cm.core.gui.model.plugin;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:46
 */
public class ReportPluginState implements PluginState {

    @Override
    public PluginState createClone() {
        PluginState clone = new ReportPluginState();
        return clone;
    }
}
