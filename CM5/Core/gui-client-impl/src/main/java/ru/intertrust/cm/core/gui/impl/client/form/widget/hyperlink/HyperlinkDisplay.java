package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.10.2014
 *         Time: 7:26
 */
public interface HyperlinkDisplay {
    void displayHyperlinks(LinkedHashMap<Id, String> items, boolean displayTooltipButton);
}
