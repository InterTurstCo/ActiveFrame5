package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.model.form.Form;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:29
 */
public class SomeActivePluginView extends PluginView {
    private final Form form;

    protected SomeActivePluginView(Plugin plugin, Form form) {
        super(plugin);
        this.form = form;
    }

    @Override
    public IsWidget getViewWidget() {
        return new FormPanel(form);
    }
}
