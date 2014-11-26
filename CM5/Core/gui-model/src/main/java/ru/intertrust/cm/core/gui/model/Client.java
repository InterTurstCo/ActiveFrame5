package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 26.11.2014
 *         Time: 15:29
 */
public interface Client extends Dto {
    String getTimeZoneId();

    String getDescriptor();
}
