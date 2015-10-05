package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2015
 *         Time: 13:49
 */
public interface AccessChanges extends Dto {
    boolean clearFullAccessLog();

    int getObjectsQty();
}
