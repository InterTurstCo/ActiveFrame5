package ru.intertrust.cm.core.dao.impl;

import java.security.Principal;
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.IdpAdminService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.dao.api.RequestInfo;
import ru.intertrust.cm.core.dao.api.TicketService;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Реализация доступа к текущему пользователю, вошедшему в систему.
 *
 * @author atsvetkov
 */
public class CurrentUserAccessorImpl implements CurrentUserAccessor {

    private final static Logger logger = LoggerFactory.getLogger(CurrentUserAccessorImpl.class);

    private EJBContext ejbContext;

    private final ThreadLocal<String> ticketPerson = new ThreadLocal<>();

    private ThreadLocal<RequestInfo> requestInfo = new ThreadLocal<RequestInfo>();

    @Autowired
    private TicketService ticketService;

    private EJBContext getEjbContext() {
        if (ejbContext == null) {
            try {
                InitialContext ic = new InitialContext();
                ejbContext = (SessionContext) ic.lookup("java:comp/EJBContext");
            } catch (NamingException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return ejbContext;
    }

    /**
     * @return логин текущего пользователя или <code>null</code>, если пользователя нет в EJB контексте или при возникновения исключения
     */
    @Override
    public String getCurrentUser() {
        String result = null;
        try {
            EJBContext ejbContext = getEjbContext();

            if (logger.isDebugEnabled()) {
                Principal principal = ejbContext.getCallerPrincipal();
                logger.debug("Caller principal: " + (principal != null ? "present" : "absent"));
                if (principal != null) {
                    logger.debug("Principal name: " + principal.getName());
                }
                logger.debug("Roles: cm_user=" + (ejbContext.isCallerInRole("cm_user") ? "YES" : "no")
                        + "; system=" + (ejbContext.isCallerInRole("system") ? "YES" : "no"));
            }
            if (ejbContext.getContextData() != null
                    && Boolean.TRUE.equals(ejbContext.getContextData().get(INITIAL_DATA_LOADING))) {
                return null;
            }
            String principalName = ejbContext.getCallerPrincipal().getName();
            if (!principalName.equals("guest") && !principalName.equals("anonymous") && !principalName.equals("system")) {
                result = principalName;
            } else if (ejbContext.isCallerInRole("system") || principalName.equals("system")) {
                result = "admin";
            } else if (ejbContext.isCallerInRole("cm_user")) {
                result = ejbContext.getCallerPrincipal().getName();
            }
            // Вычисление пользователя по UNID
            if (result != null && isUuid(result)) {
                // Получаем персону по UUID
                DomainObject person = getPersonServiceDao().findPersonByAltUid(result, IdpAdminService.IDP_ALTER_UID_TYPE);
                if (person == null) {
                    result = null;
                } else {
                    result = person.getString("Login");
                }
            }
        } catch (Exception e) {
            logger.error("Error getting current user", e);
        }
        return result != null ? result : ticketPerson.get();
    }

    private boolean isUuid(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Возвращает идентификатор текущего пользователя. Возвращает null, если невозможно получить пользователя из EJB
     * контекста.
     *
     * @return идентификатор текущего пользователя.
     */
    @Override
    public Id getCurrentUserId() {
        String login = getCurrentUser();
        if (login == null) {
            return null;
        }
        try {
            return getPersonServiceDao().findPersonByLogin(login).getId();
        } catch (Exception e) {
            logger.info("Error getting current user", e);
        }
        return null;
    }

    private PersonServiceDao getPersonServiceDao() {
        return SpringApplicationContext.getContext().getBean(PersonServiceDao.class);
    }

    @Override
    public void setTicket(String ticket) {
        ticketPerson.set(ticketService.checkTicket(ticket));
    }

    @Override
    public void cleanTicket() {
        ticketPerson.remove();
    }

    @Override
    public RequestInfo getRequestInfo() {
        return requestInfo.get();
    }

    @Override
    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo.set(requestInfo);
    }
}
