package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.13
 *         Time: 13:05
 */
@DeclareRoles("cm_user")
@RolesAllowed("cm_user")
public class AbstractGuiServiceImpl {
    @Autowired
    protected ApplicationContext applicationContext;

    @Resource
    protected SessionContext sessionContext;

    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @EJB
    protected CrudService crudService;

    @EJB
    protected CollectionsService collectionsService;

    protected <T extends ComponentHandler> T obtainHandler(String componentName) {
        boolean containsHandler = applicationContext.containsBean(componentName);
        return containsHandler ? (T) applicationContext.getBean(componentName) : null;
    }
}
