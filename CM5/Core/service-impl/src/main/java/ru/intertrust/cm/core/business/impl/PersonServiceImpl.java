package ru.intertrust.cm.core.business.impl;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.model.RemoteSuitableException;

@Stateless(name = "PersonService")
@Local(PersonService.class)
@Remote(PersonService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PersonServiceImpl implements PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    @Autowired
    private PersonServiceDao personServiceDao;
    
    @Autowired
    private CurrentUserAccessor currentUserAccessor;    

    @Override
    public DomainObject findPersonByLogin(String login) {
        try {
            return personServiceDao.findPersonByLogin(login);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public DomainObject getCurrentPerson() {
        return findPersonByLogin(getCurrentPersonUid());
    }

    public String getCurrentPersonUid() {
        return currentUserAccessor.getCurrentUser();
    }
}
