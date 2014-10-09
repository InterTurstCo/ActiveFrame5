package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.10.2014
 *         Time: 6:40
 */
public interface TitleBuilder extends ComponentHandler{
    PopupTitlesHolder buildPopupTitles(LinkedFormConfig config, DomainObject root);
}
