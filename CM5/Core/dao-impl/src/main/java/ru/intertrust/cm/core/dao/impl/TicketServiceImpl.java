package ru.intertrust.cm.core.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.TicketService;
import ru.intertrust.cm.core.model.FatalException;

@Stateless(name="TicketService")
@Local(TicketService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TicketServiceImpl implements TicketService {
    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Override
    public String createTicket() {
        AccessToken accessToken = accessService.createSystemAccessToken(this.getClass().getName());

        String result = UUID.randomUUID().toString();

        GenericDomainObject ticket = new GenericDomainObject();
        ticket.setTypeName("ticket");
        Date currentDate = new Date();
        ticket.setCreatedDate(currentDate);
        ticket.setModifiedDate(currentDate);

        ticket.setString("ticket", result);
        ticket.setReference("person", currentUserAccessor.getCurrentUserId());
        domainObjectDao.save(ticket, accessToken);
        return result;
    }

    @Override
    public String checkTicket(String ticket) {
        AccessToken accessToken = accessService.createSystemAccessToken(this.getClass().getName());
        Map<String, Value> key = new HashMap<String, Value>();
        key.put("ticket", new StringValue(ticket));
        DomainObject ticketObject = domainObjectDao.findByUniqueKey("ticket", key, accessToken);
        if (ticketObject != null) {
            DomainObject personObject = domainObjectDao.find(ticketObject.getReference("person"), accessToken);
            domainObjectDao.delete(ticketObject.getId(), accessToken);
            return personObject.getString("login");
        } else {
            throw new FatalException("Ticket is invalid");
        }
    }

}
