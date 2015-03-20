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

/**
 * Created by tbilyi on 31.01.14.
 */
public class ExtSearchDialogBox extends DialogBox {
    private static final String DEFAULT_WIDTH = "650px";
    private static final String DEFAULT_HEIGHT = "600px";
    private static final int MINIMAL_HEIGHT = 270;
    private static final int MINIMAL_WIDTH= 300;
    private static final int CONTENT_HEIGHT_OFFSET = 230;
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
                        view.getScrollSearchForm().setHeight(height - CONTENT_HEIGHT_OFFSET + "px");
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
        resizablePanel = new LeftSideDialogBoxResizablePanel(this,MINIMAL_WIDTH, MINIMAL_HEIGHT, true);
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
        resizablePanel.setSize(getWidth(popupConfig), getHeight(popupConfig));
        resizablePanel.setResizable(isResizable(popupConfig));
        resizablePanel.wrapWidget(wrapper);

    }

    private String getHeight(ExtendedSearchPopupConfig popupConfig){
        if(popupConfig == null || popupConfig.getDialogWindowConfig() == null){
            return DEFAULT_HEIGHT;
        } else {
            return popupConfig.getDialogWindowConfig().getHeight() == null ? DEFAULT_HEIGHT
                    : popupConfig.getDialogWindowConfig().getHeight();
        }
    }
    private String getWidth(ExtendedSearchPopupConfig popupConfig){
        if(popupConfig == null || popupConfig.getDialogWindowConfig() == null){
            return DEFAULT_WIDTH;
        } else {
            return popupConfig.getDialogWindowConfig().getWidth() == null ? DEFAULT_WIDTH
                    : popupConfig.getDialogWindowConfig().getWidth();
        }
    }
    private boolean isResizable (ExtendedSearchPopupConfig popupConfig){
        if(popupConfig == null || popupConfig.getDialogWindowConfig() == null){
            return false;
        } else {
            return popupConfig.getDialogWindowConfig().isResizable();
        }
    }
}
