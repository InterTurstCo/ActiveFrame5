package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.gui.impl.client.event.DomainObjectTypeSelectedEvent;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.2014
 *         Time: 6:58
 */
public class SelectTypePopup extends PopupPanel {
    private EventBus eventBus;

    public SelectTypePopup(CreatedObjectsConfig config, EventBus eventBus) {
        super(true, false);
        this.eventBus = eventBus;
        initPopup(config);
    }

    private void initPopup(CreatedObjectsConfig config) {
        AbsolutePanel header = new AbsolutePanel();
        header.setStyleName("srch-corner");
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("type-popup");
        container.getElement().getStyle().clearOverflow();
        Panel body = createBody(config);
        container.add(header);
        container.add(body);
        this.add(container);

    }

    private Panel createBody(CreatedObjectsConfig config) {
        List<CreatedObjectConfig> createdObjectConfigs = config.getCreateObjectConfigs();
        VerticalPanel result = new VerticalPanel();
        for (CreatedObjectConfig createdObjectConfig : createdObjectConfigs) {
            result.add(createItem(createdObjectConfig));
        }
        return result;
    }

    private Widget createItem(final CreatedObjectConfig createdObjectConfig) {
        Label result = new Label(createdObjectConfig.getText());
        result.setStyleName("clickableHierarchyLabel");
        result.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEventFromSource(new DomainObjectTypeSelectedEvent(createdObjectConfig.getDomainObjectType()),
                        SelectTypePopup.this);
                SelectTypePopup.this.hide();
            }
        });
        return result;
    }

}
