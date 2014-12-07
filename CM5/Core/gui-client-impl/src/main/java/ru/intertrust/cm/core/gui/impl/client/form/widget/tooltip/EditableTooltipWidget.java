package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.form.widget.EventBlocker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.TooltipCallback;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.TooltipWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.10.2014
 *         Time: 17:33
 */
public abstract class EditableTooltipWidget extends TooltipWidget {

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
    protected void tryToPoolFromTooltipContent() {
        TooltipWidgetState state = getInitialData();
        boolean tooltipAvailable = WidgetUtil.shouldDrawTooltipButton(state);
        if (!tooltipAvailable) {
            return;
        }
        LinkedHashMap<Id, String> tooltipValues = state.getTooltipValues();
        if (tooltipValues == null) {
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    drawItemFromTooltipContent();
                }
            });
        } else {
            drawItemFromTooltipContent();
        }
    }

    protected Map.Entry<Id, String> pollItemFromTooltipContent() {
        TooltipWidgetState state = getInitialData();
        Iterator<Map.Entry<Id, String>> iterator = state.getTooltipValues().entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<Id, String> entry = iterator.next();
            iterator.remove();
            if (!iterator.hasNext()) {
                removeTooltipButton();

            }
            return entry;
        }
        return null;
    }

    protected abstract void removeTooltipButton();

    protected abstract void drawItemFromTooltipContent();
}
