package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.FormatRowsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.ParsedRowsList;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserItem;
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
        Id rootId = context.getFormObjects().getRootNode().getDomainObject().getId();
        state.setRootId(rootId);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        List<DomainObject> domainObjects;
        if (!selectedIds.isEmpty()) {
            domainObjects = crudService.find(selectedIds);
        } else {
            domainObjects = Collections.emptyList();
        }
        ArrayList<TableBrowserItem> items = new ArrayList<TableBrowserItem>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Pattern pattern = createDefaultRegexPattern();
        Matcher matcher = pattern.matcher(selectionPatternConfig.getValue());

        for (DomainObject domainObject : domainObjects) {
            TableBrowserItem item = new TableBrowserItem();
            item.setId(domainObject.getId());
            item.setStringRepresentation(format(domainObject, matcher));
            items.add(item);
        }
        state.setTableBrowserItems(items);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig) ;
        state.setSingleChoice(singleChoice);

        return state;
    }

    public ParsedRowsList fetchParsedRows(Dto inputParams) {
        FormatRowsRequest formatRowsRequest = (FormatRowsRequest) inputParams;
        List<Id> idsToParse = formatRowsRequest.getIdsShouldBeFormatted();
        List<DomainObject> domainObjects = crudService.find(idsToParse);
        Pattern pattern = createDefaultRegexPattern();

        Matcher selectionMatcher = pattern.matcher(formatRowsRequest.getSelectionPattern());

        ArrayList<TableBrowserItem> items = new ArrayList<>();

        for (DomainObject domainObject : domainObjects) {
            TableBrowserItem item = new TableBrowserItem();
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
