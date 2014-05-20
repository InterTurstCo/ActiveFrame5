package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class LinkedDomainObjectHyperlinkState extends LinkEditingWidgetState {

    private FormPluginConfig config;
    private String domainObjectType;
    private String selectionPattern;
    private SelectionStyleConfig selectionStyleConfig;
    private List<HyperlinkItem> hyperlinkItems;

    public FormPluginConfig getConfig() {
        return config;
    }

    public void setConfig(FormPluginConfig config) {
        this.config = config;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public String getSelectionPattern() {
        return selectionPattern;
    }

    public void setSelectionPattern(String selectionPattern) {
        this.selectionPattern = selectionPattern;
    }

    public SelectionStyleConfig getSelectionStyleConfig() {
        return selectionStyleConfig;
    }

    public void setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
    }

    public List<HyperlinkItem> getHyperlinkItems() {
        return hyperlinkItems;
    }

    public void setHyperlinkItems(List<HyperlinkItem> hyperlinkItems) {
        this.hyperlinkItems = hyperlinkItems;
    }

    @Override
    public ArrayList<Id> getIds() {
        return null;
    }
}
