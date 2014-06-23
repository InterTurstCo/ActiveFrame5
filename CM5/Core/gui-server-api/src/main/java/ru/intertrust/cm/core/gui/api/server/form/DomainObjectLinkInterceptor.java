package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * @author Denis Mitavskiy
 *         Date: 18.06.2014
 *         Time: 10:46
 */
public interface DomainObjectLinkInterceptor extends ComponentHandler {
    boolean beforeLink(DomainObjectLinkContext context);

    boolean beforeUnlink(DomainObjectLinkContext context);
}
