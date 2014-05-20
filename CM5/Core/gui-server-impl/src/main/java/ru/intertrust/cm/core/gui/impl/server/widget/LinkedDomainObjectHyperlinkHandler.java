package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkItem;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkHandler extends WidgetHandler {
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    private CrudService crudService;

    @Override
    public LinkedDomainObjectHyperlinkState getInitialState(WidgetContext context) {
        LinkedDomainObjectHyperlinkConfig widgetConfig = context.getWidgetConfig();
        LinkedDomainObjectHyperlinkState state = new LinkedDomainObjectHyperlinkState();
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        if (!selectedIds.isEmpty()) {
            Id id = selectedIds.get(0);
            FormPluginConfig config = getFormPluginConfig(id);
            state.setConfig(config);
            DomainObject firstDomainObject = crudService.find(id);
            state.setDomainObjectType(firstDomainObject.getTypeName());
            List<HyperlinkItem> hyperlinkItems = new ArrayList<>();
            for (Id selectedId : selectedIds) {
                DomainObject domainObject = crudService.find(selectedId);
                String selectionPattern = widgetConfig.getPatternConfig().getValue();
                state.setSelectionPattern(selectionPattern);
                String representation = buildStringRepresentation(domainObject, selectionPattern);
                HyperlinkItem hyperlinkItem = new HyperlinkItem(selectedId, representation);
                hyperlinkItems.add(hyperlinkItem);

            }
            state.setHyperlinkItems(hyperlinkItems);
        }
        return state;
    }

    private String buildStringRepresentation(DomainObject domainObject, String selectionPattern) {
        Matcher matcher = FormatHandler.pattern.matcher(selectionPattern);
        String representation = formatHandler.format(domainObject, matcher);
        return representation;
    }

    private FormPluginConfig getFormPluginConfig(Id id) {
        FormPluginConfig formConfig = new FormPluginConfig();
        formConfig.getPluginState().setEditable(false);
        formConfig.setDomainObjectId(id);
        return formConfig;
    }


    @Override
    public Value getValue(WidgetState state) {
        return null;
    }
}
