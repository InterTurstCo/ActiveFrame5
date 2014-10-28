package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.ConfigurationDeployedItem;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.14
 *         Time: 17:15
 */
public class ConfigDeployingResultPopup extends PopupPanel {
    private boolean refreshGui = false;

    public ConfigDeployingResultPopup(List<ConfigurationDeployedItem> configurationDeployedItems) {
        initPopup(configurationDeployedItems);
    }

    private void initPopup(List<ConfigurationDeployedItem> configurationDeployedItems) {

        this.setStyleName("popup-panel");

        AbsolutePanel header = new AbsolutePanel();
//        header.setStyleName("srch-corner");
        AbsolutePanel body = new AbsolutePanel();
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("settings-popup");
        container.getElement().getStyle().clearOverflow();
        drawConfigurationDeployedItems(body, configurationDeployedItems);

        container.add(header);
        container.add(body);
        Button submit = new Button("OK");
        submit.setStyleName("darkButton");
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (refreshGui) {
                    Window.Location.reload();
                }
                ConfigDeployingResultPopup.this.hide();
            }
        });
        container.add(submit);
        this.add(container);

    }

    private void drawConfigurationDeployedItems(AbsolutePanel body, List<ConfigurationDeployedItem> configurationDeployedItems) {
        for (ConfigurationDeployedItem configurationDeployedItem : configurationDeployedItems) {
            AbsolutePanel itemPanel = drawConfigurationDeployedItemPanel(configurationDeployedItem);
            body.add(itemPanel);
        }
    }

    private AbsolutePanel drawConfigurationDeployedItemPanel(ConfigurationDeployedItem item) {
        AbsolutePanel itemPanel = new AbsolutePanel();
        itemPanel.addStyleName("configuration-deployed-item");
        Label filename = new Label(item.getFileName());
        itemPanel.add(filename);
        Label message = new Label(item.getMessage());
        itemPanel.add(message);
        ImageResource imageResource = getStatusImageResource(item);
        Image status = new Image(imageResource);
        itemPanel.add(status);
        return itemPanel;

    }

    private ImageResource getStatusImageResource(ConfigurationDeployedItem item) {
        boolean success = item.isSuccess();
        if (success) {
            refreshGui = true;
            return GlobalThemesManager.getCurrentTheme().doneIm();
        }
        return GlobalThemesManager.getCurrentTheme().failedIm();
    }
}
