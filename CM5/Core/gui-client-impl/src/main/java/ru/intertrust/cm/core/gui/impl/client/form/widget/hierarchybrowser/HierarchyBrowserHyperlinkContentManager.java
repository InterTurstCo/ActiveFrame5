package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public abstract class HierarchyBrowserHyperlinkContentManager {
    protected Id id;
    protected String collectionName;
    protected HierarchyBrowserConfig config;

    protected HierarchyBrowserHyperlinkContentManager(Id id, String collectionName, HierarchyBrowserConfig config) {
        this.collectionName = collectionName;
        this.config = config;
        this.id = id;
    }

    protected abstract void updateHyperlink();

    protected NodeCollectionDefConfig getNodeConfigForRedraw(String collectionName, NodeCollectionDefConfig nodeConfig) {
        if (nodeConfig == null) {
            return null;
        }

        if (collectionName.equalsIgnoreCase(nodeConfig.getCollection())){
            return nodeConfig;
        }
        else return getNodeConfigForRedraw(collectionName, nodeConfig.getNodeCollectionDefConfig());
    }

}
