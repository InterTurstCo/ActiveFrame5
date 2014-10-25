package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.UIObject;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.gui.impl.client.event.DomainObjectTypeSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.DomainObjectTypeSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserAddItemClickEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.SelectTypePopup;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.2014
 *         Time: 6:51
 */
public class HierarchyBrowserAddClickHandler implements ClickHandler {
    private Id parentId;
    private String parentCollectionName;
    private NodeCollectionDefConfig nodeConfig;
    private EventBus eventBus;
    private UIObject uiObject;
    private SelectTypePopup selectTypePopup;

    public HierarchyBrowserAddClickHandler(Id parentId, String parentCollectionName, NodeCollectionDefConfig nodeConfig,
                                           EventBus eventBus, UIObject uiObject) {
        this.parentId = parentId;
        this.parentCollectionName = parentCollectionName;
        this.nodeConfig = nodeConfig;
        this.eventBus = eventBus;
        this.uiObject = uiObject;
        initEventHandler();

    }

    private void initEventHandler() {
        eventBus.addHandler(DomainObjectTypeSelectedEvent.TYPE, new DomainObjectTypeSelectedEventHandler() {
            @Override
            public void onDomainObjectTypeSelected(DomainObjectTypeSelectedEvent event) {
                if (event.getSource().equals(selectTypePopup)) {
                    String domainObjectType = event.getDomainObjectType();
                    eventBus.fireEvent(new HierarchyBrowserAddItemClickEvent(parentId, parentCollectionName,
                            domainObjectType, nodeConfig));

                }
            }
        });
    }

    @Override
    public void onClick(ClickEvent event) {
        CreatedObjectsConfig createdObjectsConfig = nodeConfig.getCreatedObjectsConfig();
        List<CreatedObjectConfig> createdObjectConfigs = nodeConfig.getCreatedObjectsConfig().getCreateObjectConfigs();
        if (createdObjectConfigs.size() == 1) {
            String domainObjectType = createdObjectConfigs.get(0).getDomainObjectType();
            eventBus.fireEvent(new HierarchyBrowserAddItemClickEvent(parentId, parentCollectionName, domainObjectType,
                    nodeConfig));
        } else {
            selectTypePopup = new SelectTypePopup(createdObjectsConfig, eventBus);
            selectTypePopup.showRelativeTo(uiObject);
        }
    }

}
