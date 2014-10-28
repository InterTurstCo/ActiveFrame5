package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.LinkEditingWidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.EventBlocker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.TooltipCallback;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.TooltipWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 21:31
 */
public abstract class TooltipWidget extends BaseWidget implements ShowTooltipEventHandler,HasLinkedFormMappings {
    protected EventBus localEventBus = new SimpleEventBus();
    protected void fetchWidgetItems() {
        final TooltipWidgetState state = getInitialData();
        LinkedHashMap<Id, String> previousTooltipValues = state.getTooltipValues();
        if (previousTooltipValues != null) {
            handleItemsForTooltipContent(previousTooltipValues);
        } else {
            WidgetItemsRequest widgetItemsRequest = createRequest();
            Command command = new Command("fetchWidgetItems", getTooltipHandlerName(), widgetItemsRequest);
            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                @Override
                public void onSuccess(Dto result) {
                    WidgetItemsResponse list = (WidgetItemsResponse) result;
                    LinkedHashMap<Id, String> tooltipValues = list.getListValues();
                    state.setTooltipValues(tooltipValues);
                    handleItemsForTooltipContent(tooltipValues);
                }

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("something was going wrong while obtaining rows");
                }
            });
        }

    }

    private WidgetItemsRequest createRequest() {
        TooltipWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        WidgetItemsRequest request = new WidgetItemsRequest();
        request.setSelectionPattern(config.getSelectionPatternConfig().getValue());
        request.setSelectedIds(state.getIds());
        request.setCollectionName(config.getCollectionRefConfig().getName());
        request.setFormattingConfig(config.getFormattingConfig());
        request.setSelectionSortCriteriaConfig(config.getSelectionSortCriteriaConfig());
        request.setSelectionFiltersConfig(config.getSelectionFiltersConfig());
        return request;
    }

    protected void fetchWidgetItems(final TooltipCallback tooltipCallback) {
        WidgetItemsRequest widgetItemsRequest = createRequest();
        Command command = new Command("fetchWidgetItems", getTooltipHandlerName(), widgetItemsRequest);
        final HandlerRegistration handlerRegistration = Event.addNativePreviewHandler(new EventBlocker(impl));
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                handlerRegistration.removeHandler();
                WidgetItemsResponse list = (WidgetItemsResponse) result;
                LinkedHashMap<Id, String> tooltipValues = list.getListValues();
                TooltipWidgetState state = getInitialData();
                state.setTooltipValues(tooltipValues);
                tooltipCallback.perform();
            }

            @Override
            public void onFailure(Throwable caught) {
                handlerRegistration.removeHandler();
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }

    private void handleItemsForTooltipContent(LinkedHashMap<Id, String> listValues) {
        TooltipWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        SelectionStyleConfig styleConfig = config.getSelectionStyleConfig();
        if (isEditable()) {
            EditableWidgetTooltip tooltip = new EditableWidgetTooltip(styleConfig, localEventBus, isDisplayingAsHyperlink(),
                    state.getTypeTitleMap(), this);
            TooltipSizer.setWidgetBounds(config, tooltip);
            tooltip.displayItems(listValues);
            tooltip.showRelativeTo(impl);
        } else {
            NoneEditableTooltip noneEditableTooltip = new NoneEditableTooltip(styleConfig, localEventBus,
                    isDisplayingAsHyperlink(), state.getTypeTitleMap(), this);
            TooltipSizer.setWidgetBounds(config, noneEditableTooltip);
            noneEditableTooltip.displayItems(listValues);
            noneEditableTooltip.showRelativeTo(impl);
        }
    }

    protected boolean shouldDrawTooltipButton() {
        return shouldDrawTooltipButton(0);
    }

    protected boolean shouldDrawTooltipButton(int delta) {
        TooltipWidgetState state = getInitialData();
        LinkEditingWidgetConfig config = (LinkEditingWidgetConfig) state.getWidgetConfig();
        return config.getSelectionFiltersConfig() != null &&
                config.getSelectionFiltersConfig().getRowLimit() != -1
                && state.getSelectedIds() != null
                && state.getSelectedIds().size() + delta > config.getSelectionFiltersConfig().getRowLimit();
    }

    protected boolean isDisplayingAsHyperlink() {
        TooltipWidgetState state = getInitialData();
        return state.isDisplayingAsHyperlinks();
    }

    protected abstract String getTooltipHandlerName();

    @Override
    public void showTooltip(ShowTooltipEvent event) {
        fetchWidgetItems();
    }
}
