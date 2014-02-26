package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DialogBox;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;

/**
 * Created by tbilyi on 31.01.14.
 */
public class ExtSearchDialogBox extends DialogBox {

    private PluginPanel formPluginPanel;
    PluginPanel extendSearchPluginPanel;
    private AbsolutePanel rootExtSearchPanel;
    private AbsolutePanel corner;
    private AbsolutePanel wrapper;
    private AbsolutePanel headerPanel;
    private AbsolutePanel buttonPanel;

    private void init() {
        extendSearchPluginPanel = new PluginPanel();
        final ExtendedSearchPlugin extendedSearchPlugin = ComponentRegistry.instance.get("extended.search.plugin");
        extendSearchPluginPanel.open(extendedSearchPlugin);

        extendedSearchPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {

                ExtendedSearchPluginView view = (ExtendedSearchPluginView) extendedSearchPlugin.getView();
                view.setSearchPopup(ExtSearchDialogBox.this);
            }
        });

        //extendSearchPluginPanel.setSize("650px", "750px");
    }

    public ExtSearchDialogBox() {
        rootExtSearchPanel = new AbsolutePanel();

        corner = new AbsolutePanel();
        wrapper = new AbsolutePanel();
        headerPanel = new AbsolutePanel();
        rootExtSearchPanel.add(corner);
        rootExtSearchPanel.add(wrapper);
        wrapper.add(headerPanel);
        init();
        wrapper.add(extendSearchPluginPanel);
        this.add(rootExtSearchPanel);
        setStyleForExtSearch();
    }

    private void setStyleForExtSearch() {
        this.addStyleName("srch-dialog-box-wrapper");
        this.getElement().getStyle().setZIndex(999);
        this.removeStyleName("gwt-DialogBox");
        rootExtSearchPanel.addStyleName("srch-root-ext-search-panel");
        corner.addStyleName("srch-corner");
        wrapper.addStyleName("srch-wrapper");
        headerPanel.addStyleName("srch-header");
    }


    private void closeDialog() {
        this.removeFromParent();
        //this.hide();
    }
}
