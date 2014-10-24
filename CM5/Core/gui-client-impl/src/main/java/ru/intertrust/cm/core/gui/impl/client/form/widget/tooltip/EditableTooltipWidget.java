package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.TooltipCallback;
import ru.intertrust.cm.core.gui.model.form.widget.TooltipWidgetState;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.10.2014
 *         Time: 17:33
 */
public abstract class EditableTooltipWidget extends TooltipWidget {

    protected abstract void removeTooltipButton();

    protected abstract void drawItemFromTooltipContent();

    protected void tryToPoolFromTooltipContent() {
        TooltipWidgetState state = getInitialData();
        boolean tooltipAvailable = shouldDrawTooltipButton();
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
}
