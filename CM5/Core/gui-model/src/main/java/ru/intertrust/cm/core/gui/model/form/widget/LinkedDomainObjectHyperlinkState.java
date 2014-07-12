package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class LinkedDomainObjectHyperlinkState extends TooltipWidgetState<LinkedDomainObjectHyperlinkConfig> {

    private FormPluginConfig config;
    private LinkedDomainObjectHyperlinkConfig widgetConfig;
    private String domainObjectType;

    private ArrayList<Id> selectedIds;
    public FormPluginConfig getConfig() {
        return config;
    }

    public void setConfig(FormPluginConfig config) {
        this.config = config;
    }

    public LinkedDomainObjectHyperlinkConfig getWidgetConfig() {
        return widgetConfig;
    }

    @Override
    public Set<Id> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public void setWidgetConfig(LinkedDomainObjectHyperlinkConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }


    public void setSelectedIds(ArrayList<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds;
    }
}
