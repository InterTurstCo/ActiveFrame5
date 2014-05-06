package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;

import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public abstract class HierarchyBrowserHyperlinkContentManager {
    protected Id id;
    protected String collectionName;
    protected HierarchyBrowserConfig config;
    protected Map<String, NodeCollectionDefConfig> collectionNameNodeMap;
    protected HierarchyBrowserHyperlinkContentManager(Id id, String collectionName, HierarchyBrowserConfig config,
                                                      Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        this.collectionName = collectionName;
        this.config = config;
        this.id = id;
        this.collectionNameNodeMap = collectionNameNodeMap;
    }

    protected abstract void updateHyperlink();


}
