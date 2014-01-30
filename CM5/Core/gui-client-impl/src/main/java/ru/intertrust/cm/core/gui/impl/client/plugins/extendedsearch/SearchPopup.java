package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class SearchPopup extends PopupPanel {
    private PluginPanel formPluginPanel;
    private  AbsolutePanel buttonsPanel;

    public SearchPopup(){
        init();
    }

    private void init(){
        // Enable animation.
        this.setAnimationEnabled(true);
        this.setModal(true);
        this.addStyleName("dialog-box-body");

        this.removeStyleName("gwt-PopupPanel");
        Label label = new Label("Поиск");
        label.addStyleName("form-header-message");
        label.removeStyleName("gwt-Label");
        AbsolutePanel panel = new AbsolutePanel();
        panel.addStyleName("form-dialog-box-content");
        SimplePanel header = new SimplePanel();
        header.addStyleName("dialog-box-header");
        header.add(label);
        panel.add(header);

        PluginPanel extendSearchPluginPanel = new PluginPanel();
        final ExtendedSearchPlugin extendedSearchPlugin = ComponentRegistry.instance.get("extended.search.plugin");
        extendSearchPluginPanel.open(extendedSearchPlugin);

        extendedSearchPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {

            ExtendedSearchPluginView view = (ExtendedSearchPluginView) extendedSearchPlugin.getView();
                view.setSearchPopup(SearchPopup.this);
            }
        });

        extendSearchPluginPanel.setSize("900px", "400px");
        panel.add(extendSearchPluginPanel);

        buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("search-buttons-panel");
        panel.add(buttonsPanel);
        this.add(panel);
    }
}
