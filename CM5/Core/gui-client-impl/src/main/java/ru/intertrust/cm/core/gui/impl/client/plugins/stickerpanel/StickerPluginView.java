package ru.intertrust.cm.core.gui.impl.client.plugins.stickerpanel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

public class StickerPluginView extends PluginView {
    public StickerPluginView(StickerPlugin stickerPlugin) {
        super(stickerPlugin);
    }
    @Override
    protected IsWidget getViewWidget() {
        FlowPanel stickerContainer = new FlowPanel();
        stickerContainer.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        stickerContainer.getElement().getStyle().setProperty("marginLeft", "5px");
        stickerContainer.getElement().getStyle().setProperty("marginRight", "5px");
        Button stickerBtn = new Button("sticker");
        stickerContainer.add(stickerBtn);
        return stickerContainer;
    }
}
