package ru.intertrust.cm.core.config.gui.action;

import java.io.Serializable;

/**
 * Marker of action config.
 * @author Sergey.Okolot
 *         Created on 15.04.2014 11:57.
 */
public abstract class AbstractActionEntryConfig implements Serializable {

    public abstract String getId();

    public abstract Integer getOrder();

    public abstract boolean isRendered();

    public abstract String getGroupId();
}
