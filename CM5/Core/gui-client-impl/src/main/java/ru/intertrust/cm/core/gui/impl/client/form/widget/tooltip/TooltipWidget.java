package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkEditingWidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.TooltipWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 21:31
 */
public abstract class TooltipWidget extends BaseWidget {
    protected EventBus localEventBus = new SimpleEventBus();

    protected void fetchWidgetItems() {
        TooltipWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        WidgetItemsRequest widgetItemsRequest = new WidgetItemsRequest();
        widgetItemsRequest.setSelectionPattern(config.getSelectionPatternConfig().getValue());
        widgetItemsRequest.setSelectedIds(state.getIds());
        widgetItemsRequest.setCollectionName(config.getCollectionRefConfig().getName());
        widgetItemsRequest.setFormattingConfig(config.getFormattingConfig());
        widgetItemsRequest.setDefaultSortCriteriaConfig(config.getDefaultSortCriteriaConfig());
        widgetItemsRequest.setSelectionFiltersConfig(config.getSelectionFiltersConfig());
        Command command = new Command("fetchWidgetItems", getTooltipHandlerName(), widgetItemsRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                WidgetItemsResponse list = (WidgetItemsResponse) result;
                LinkedHashMap<Id, String> listValues = list.getListValues();
                handleItemsForTooltipContent(listValues);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });

    }

    private void handleItemsForTooltipContent(LinkedHashMap<Id, String> listValues) {
        TooltipWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        SelectionStyleConfig styleConfig = config.getSelectionStyleConfig();

        if (isEditable()) {
            Set<Id> ids = state.getSelectedIds();
            EditableWidgetTooltip tooltip = new EditableWidgetTooltip(styleConfig, localEventBus, isDisplayingAsHyperlink(), ids);
            TooltipSizer.setWidgetBounds(config, tooltip);
            tooltip.displayItems(listValues);
            tooltip.showRelativeTo(impl);
        } else {
            NoneEditableTooltip noneEditableTooltip = new NoneEditableTooltip(styleConfig, localEventBus, isDisplayingAsHyperlink());
            TooltipSizer.setWidgetBounds(config, noneEditableTooltip);
            noneEditableTooltip.displayItems(listValues);
            noneEditableTooltip.showRelativeTo(impl);
        }
    }

    protected boolean shouldDrawTooltipButton() {
        TooltipWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        return config.getSelectionFiltersConfig() != null &&
                config.getSelectionFiltersConfig().getRowLimit() != 0 && !state.getSelectedIds().isEmpty();
    }

    protected boolean isDisplayingAsHyperlink() {
        TooltipWidgetState state = getInitialData();
        return state.isDisplayingAsHyperlinks();
    }

    protected abstract String getTooltipHandlerName();

    public class ShowTooltipHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            fetchWidgetItems();
        }
    }

}
