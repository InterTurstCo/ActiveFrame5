package ru.intertrust.cm.core.gui.impl.client.form.widget.tablebrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkNoneEditablePanel;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.11.2014
 *         Time: 8:38
 */
public class HyperlinkNoneEditablePanelViewHolder extends ViewHolder<HyperlinkNoneEditablePanel, TableBrowserState>{
    public HyperlinkNoneEditablePanelViewHolder(HyperlinkNoneEditablePanel widget) {
        super(widget);
    }

    @Override
    public void setContent(TableBrowserState state) {
        LinkedHashMap<Id, String> listValues = state.getListValues();
        boolean shouldDrawTooltipButton = WidgetUtil.shouldDrawTooltipButton(state);
        boolean displayHyperlinks = state.isDisplayingAsHyperlinks();
        widget.display(listValues, shouldDrawTooltipButton, displayHyperlinks);
    }
}
