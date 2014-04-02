package ru.intertrust.cm.core.gui.impl.client.plugins.headernotification;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * Created by lvov on 25.03.14.
 */
public class HeaderNotificationPluginView extends PluginView{
    private FlowPanel container;

    protected HeaderNotificationPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IsWidget getViewWidget() {
        container = new FlowPanel();
        containerSetStyle();

        for (int i =0; i < 5; i++){
            AbsolutePanel absolutePanel = new AbsolutePanel();
            final FocusPanel crossContainer = new FocusPanel();
            crossContainer.addStyleName("cross-button");


            absolutePanel.addStyleName("section-suggest-item");

            absolutePanel.setSize("100%", "35px");
            Label label = new Label("label "+i);
          //  label.addStyleName("header-notification");
            absolutePanel.add(label);
            absolutePanel.add(crossContainer);
            container.add(absolutePanel);

            crossContainer.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    crossContainer.getParent().removeFromParent();
                }
            });
        }

        return container;
    }

    private void containerSetStyle(){
        container.addStyleName("header-notification-style");
    }
}
