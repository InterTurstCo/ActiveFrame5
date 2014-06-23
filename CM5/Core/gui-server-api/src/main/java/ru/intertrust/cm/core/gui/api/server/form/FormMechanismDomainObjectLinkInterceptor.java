package ru.intertrust.cm.core.gui.api.server.form;

/**
 * @author Denis Mitavskiy
 *         Date: 19.06.2014
 *         Time: 15:54
 */
public class FormMechanismDomainObjectLinkInterceptor implements DomainObjectLinkInterceptor {
    @Override
    public boolean beforeLink(DomainObjectLinkContext context) {
        return true;
    }

    @Override
    public boolean beforeUnlink(DomainObjectLinkContext context) {
        return true;
    }
}
