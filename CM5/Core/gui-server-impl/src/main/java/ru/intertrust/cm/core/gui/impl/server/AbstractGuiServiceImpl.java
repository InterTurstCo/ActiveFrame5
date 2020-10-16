package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.transaction.Status;

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

    @Autowired
    protected CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @EJB
    protected CrudService crudService;

    @EJB
    protected CollectionsService collectionsService;

    @Resource
    protected EJBContext ejbContext;

    protected <T extends ComponentHandler> T obtainHandler(String componentName) {
        boolean containsHandler = applicationContext.containsBean(componentName);
        return containsHandler ? (T) applicationContext.getBean(componentName) : null;
    }

    /**
     * Возвращает строковое описание статуса текущей транзакции
     * @return строковое описание статуса текущей транзакции
     */
    public static String getTransactionStatusDescription(int status) {
        switch (status) {
            case Status.STATUS_ACTIVE:
                return "Active";
            case Status.STATUS_MARKED_ROLLBACK:
                return "Marked For Rollback";
            case Status.STATUS_PREPARED:
                return "Prepared";
            case Status.STATUS_COMMITTED:
                return "Committed";
            case Status.STATUS_ROLLEDBACK:
                return "Rolled Back";
            case Status.STATUS_NO_TRANSACTION:
                return "No Transaction";
            case Status.STATUS_PREPARING:
                return "Preparing";
            case Status.STATUS_COMMITTING:
                return "Committing";
            case Status.STATUS_ROLLING_BACK:
                return "Rolling Back";
            default:
                return "Unknown";
        }
    }
}
