package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DialogBox;
import ru.intertrust.cm.core.config.search.ExtendedSearchPopupConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.PanelResizeListener;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventListener;
import ru.intertrust.cm.core.gui.impl.client.panel.LeftSideDialogBoxResizablePanel;
import ru.intertrust.cm.core.gui.impl.client.panel.ResizablePanel;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.List;
import java.util.Map;

/**
 * Created by tbilyi on 31.01.14.
 */
public class ExtSearchDialogBox extends DialogBox {

    private PluginPanel extendSearchPluginPanel;
    private AbsolutePanel rootExtSearchPanel;
    private AbsolutePanel corner;
    private AbsolutePanel wrapper;
    private AbsolutePanel headerPanel;
    private ResizablePanel resizablePanel;

    private void initPluginPanel() {
        extendSearchPluginPanel = new PluginPanel();
        final ExtendedSearchPlugin extendedSearchPlugin = ComponentRegistry.instance.get("extended.search.plugin");
        extendSearchPluginPanel.open(extendedSearchPlugin);
        extendedSearchPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                final ExtendedSearchPluginView view = (ExtendedSearchPluginView) extendedSearchPlugin.getView();
                view.setSearchPopup(ExtSearchDialogBox.this);
                resizablePanel.addResizeListener(new PanelResizeListener() {
                    @Override
                    public void onPanelResize(int width, int height) {
                        view.setupFormSizes(height, width);
                    }
                });
            }
        });

    }

    public ExtSearchDialogBox() {
        rootExtSearchPanel = new AbsolutePanel();

        corner = new AbsolutePanel();
        wrapper = new AbsolutePanel();
        headerPanel = new AbsolutePanel();
        rootExtSearchPanel.add(corner);

        wrapper.add(headerPanel);
        initPluginPanel();
        wrapper.add(extendSearchPluginPanel);
        resizablePanel = new LeftSideDialogBoxResizablePanel(this,ExtendedSearchDialogHelper.MINIMAL_WIDTH, ExtendedSearchDialogHelper.MINIMAL_HEIGHT, true);
        rootExtSearchPanel.add(resizablePanel);
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


    public void setUpDialogWindow(ExtendedSearchPopupConfig popupConfig){
        resizablePanel.setSize(ExtendedSearchDialogHelper.getWidthInPixel(popupConfig), ExtendedSearchDialogHelper.getHeightInPixel(popupConfig));
        resizablePanel.setResizable(ExtendedSearchDialogHelper.isResizable(popupConfig));
        resizablePanel.wrapWidget(wrapper);

    }

    public void setInitedFormData(Map<String, WidgetState> extendedSearchConfiguration, List<String> searchAreas, String searchDomainObjectType) {
        if(extendSearchPluginPanel.getCurrentPlugin() != null){
            ((ExtendedSearchPlugin)extendSearchPluginPanel.getCurrentPlugin()).resetFormByInitData(extendedSearchConfiguration, searchAreas, searchDomainObjectType);
        }
    }
}
