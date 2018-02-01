package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.model.RemoteSuitableException;

@Stateless(name = "PersonManagementService")
@Local(PersonManagementService.class)
@Remote(PersonManagementService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PersonManagementServiceImpl implements PersonManagementService {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(PersonManagementServiceImpl.class);

    @Autowired
    private PersonManagementServiceDao personManagementServiceDao;

    @Autowired
    private PersonServiceDao personServiceDao;

    @Override
    public Id getPersonId(String login) {
        try {
            return personServiceDao.findPersonByLogin(login).getId();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getPersonsInGroup(Id groupId) {
        try {
            return personManagementServiceDao.getPersonsInGroup(groupId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getAllPersonsInGroup(Id groupId) {
        try {
            return personManagementServiceDao.getAllPersonsInGroup(groupId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public boolean isPersonInGroup(Id groupId, Id personId) {
        try {
            return personManagementServiceDao.isPersonInGroup(groupId, personId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getPersonGroups(Id personId) {
        try {
            return personManagementServiceDao.getPersonGroups(personId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public boolean isGroupInGroup(Id parent, Id child, boolean recursive) {
        try {
            return personManagementServiceDao.isGroupInGroup(parent, child, recursive);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getAllParentGroup(Id child) {
        try {
            return personManagementServiceDao.getAllParentGroup(child);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getChildGroups(Id parent) {
        try {
            return personManagementServiceDao.getChildGroups(parent);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DomainObject> getAllChildGroups(Id parent) {
        try {
            return personManagementServiceDao.getAllChildGroups(parent);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void addPersonToGroup(Id group, Id person) {
        try {
            personManagementServiceDao.addPersonToGroup(group, person);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void addGroupToGroup(Id parent, Id child) {
        try {
            personManagementServiceDao.addGroupToGroup(parent, child);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void remotePersonFromGroup(Id group, Id person) {
        try {
            personManagementServiceDao.remotePersonFromGroup(group, person);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public void remoteGroupFromGroup(Id parent, Id child) {
        try {
            personManagementServiceDao.remoteGroupFromGroup(parent, child);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }

    }

    @Override
    public Id getGroupId(String groupName) {
        try {
            return personManagementServiceDao.getGroupId(groupName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public DomainObject findDynamicGroup(String name, Id contectId) {
        try {
            return personManagementServiceDao.findDynamicGroup(name, contectId);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

}
