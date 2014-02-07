package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkUpdateRequest;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkUpdateResponse;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
@ComponentName("linked-domain-object-hyperlink")
public class LinkedDomainObjectHyperlinkHandler extends LinkEditingWidgetHandler {
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
            DomainObject domainObject = crudService.find(id);
            FormPluginConfig config = getFormPluginConfig(id);
            String selectionPattern = widgetConfig.getPatternConfig().getValue();
            state.setSelectionPattern(selectionPattern);
            String representation = buildStringRepresentation(domainObject, selectionPattern);
            state.setId(id);
            state.setStringRepresentation(representation);
            state.setConfig(config);
            state.setDomainObjectType(domainObject.getTypeName());
        }

        return state;
    }

    private String buildStringRepresentation(DomainObject domainObject, String selectionPattern) {
        Pattern pattern = createDefaultRegexPattern();
        Matcher matcher = pattern.matcher(selectionPattern);
        String representation = format(domainObject, matcher);
        return representation;
    }

    private FormPluginConfig getFormPluginConfig(Id id) {
        FormPluginConfig formConfig = new FormPluginConfig();
        formConfig.getPluginState().setEditable(false);
        formConfig.setDomainObjectId(id);
        return formConfig;
    }

    private Pattern createDefaultRegexPattern() {
        return Pattern.compile("\\{\\w+\\}");
    }

    public HyperlinkUpdateResponse updateHyperlink(Dto inputParams){
        HyperlinkUpdateRequest request = (HyperlinkUpdateRequest)inputParams;
        Id id = request.getId();
        DomainObject domainObject = crudService.find(id);
        String selectionPattern = request.getPattern();
        String representation = buildStringRepresentation(domainObject,selectionPattern);
        HyperlinkUpdateResponse response = new HyperlinkUpdateResponse(id, representation);
        return response;
    }
}
