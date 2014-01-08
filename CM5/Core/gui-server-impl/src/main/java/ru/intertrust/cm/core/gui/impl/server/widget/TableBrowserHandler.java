package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.FacebookStyleItem;
import ru.intertrust.cm.core.gui.model.form.widget.FormatRowsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.ParsedRowsList;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 13:15
 */
@ComponentName("table-browser")
public class TableBrowserHandler extends LinkEditingWidgetHandler {
    @Autowired
    private CrudService crudService;

    @Override
    public TableBrowserState getInitialState(WidgetContext context) {
        TableBrowserState state = new TableBrowserState();
        TableBrowserConfig widgetConfig = context.getWidgetConfig();
        state.setTableBrowserConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        List<DomainObject> domainObjects;
        if (!selectedIds.isEmpty()) {
            domainObjects = crudService.find(selectedIds);
        } else {
            domainObjects = Collections.emptyList();
        }
        ArrayList<FacebookStyleItem> items = new ArrayList<FacebookStyleItem>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Pattern pattern = createDefaultRegexPattern();
        Matcher matcher = pattern.matcher(selectionPatternConfig.getValue());
        for (DomainObject domainObject : domainObjects) {
            FacebookStyleItem item = new FacebookStyleItem();
            item.setId(domainObject.getId());
            item.setStringRepresentation(format(domainObject, matcher));
            items.add(item);
        }

        state.setSelectedItemsRepresentations(items);
        return state;
    }

    public ParsedRowsList fetchParsedRows(Dto inputParams) {
        FormatRowsRequest formatRowsRequest = (FormatRowsRequest) inputParams;
        List<Id> idsToParse = formatRowsRequest.getIdsShouldBeFormatted();
        List<DomainObject> domainObjects = crudService.find(idsToParse);
        Pattern pattern = createDefaultRegexPattern();

        Matcher selectionMatcher = pattern.matcher(formatRowsRequest.getSelectionPattern());

        ArrayList<FacebookStyleItem> items = new ArrayList<>();

        for (DomainObject domainObject : domainObjects) {
            FacebookStyleItem item = new FacebookStyleItem();
            item.setId(domainObject.getId());
            item.setStringRepresentation(format(domainObject, selectionMatcher));
            items.add(item);
        }
        ParsedRowsList parsedRows = new ParsedRowsList();
        parsedRows.setFilteredRows(items);
        return parsedRows;
    }

    private Pattern createDefaultRegexPattern() {
        return Pattern.compile("\\{\\w+\\}");
    }

}
