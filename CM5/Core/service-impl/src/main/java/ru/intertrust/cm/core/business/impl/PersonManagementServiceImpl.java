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
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.model.AccessException;
import ru.intertrust.cm.core.model.UnexpectedException;

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
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getPersonId",
                    "login:" + login, ex);
        }
    }

    @Override
    public List<DomainObject> getPersonsInGroup(Id groupId) {
        try {
            return personManagementServiceDao.getPersonsInGroup(groupId);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getPersonsInGroup",
                    "groupId:" + groupId, ex);
        }
    }

    @Override
    public List<DomainObject> getAllPersonsInGroup(Id groupId) {
        try {
            return personManagementServiceDao.getAllPersonsInGroup(groupId);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getAllPersonsInGroup",
                    "groupId:" + groupId, ex);
        }
    }

    @Override
    public boolean isPersonInGroup(Id groupId, Id personId) {
        try {
            return personManagementServiceDao.isPersonInGroup(groupId, personId);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "isPersonInGroup",
                    "groupId:" + groupId + " personId:" + personId, ex);
        }
    }

    @Override
    public List<DomainObject> getPersonGroups(Id personId) {
        try {
            return personManagementServiceDao.getPersonGroups(personId);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getPersonGroups",
                    "personId:" + personId, ex);
        }
    }

    @Override
    public boolean isGroupInGroup(Id parent, Id child, boolean recursive) {
        try {
            return personManagementServiceDao.isGroupInGroup(parent, child, recursive);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "isGroupInGroup",
                    "parent:" + parent + " child:" + child + " recursive: " + recursive, ex);
        }
    }

    @Override
    public List<DomainObject> getAllParentGroup(Id child) {
        try {
            return personManagementServiceDao.getAllParentGroup(child);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getAllParentGroup",
                    "child:" + child, ex);
        }
    }

    @Override
    public List<DomainObject> getChildGroups(Id parent) {
        try {
            return personManagementServiceDao.getChildGroups(parent);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getChildGroups",
                    "parent:" + parent, ex);
        }
    }

    @Override
    public List<DomainObject> getAllChildGroups(Id parent) {
        try {
            return personManagementServiceDao.getAllChildGroups(parent);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getAllChildGroups",
                    "parent:" + parent, ex);
        }
    }

    @Override
    public void addPersonToGroup(Id group, Id person) {
        try {
            personManagementServiceDao.addPersonToGroup(group, person);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "addPersonToGroup",
                    "group:" + group + " person:" + person, ex);
        }
    }

    @Override
    public void addGroupToGroup(Id parent, Id child) {
        try {
            personManagementServiceDao.addGroupToGroup(parent, child);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "addGroupToGroup",
                    "parent:" + parent + " child:" + child, ex);
        }
    }

    @Override
    public void remotePersonFromGroup(Id group, Id person) {
        try {
            personManagementServiceDao.remotePersonFromGroup(group, person);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "remotePersonFromGroup",
                    "group:" + group + " person:" + person, ex);
        }
    }

    @Override
    public void remoteGroupFromGroup(Id parent, Id child) {
        try {
            personManagementServiceDao.remoteGroupFromGroup(parent, child);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "remoteGroupFromGroup",
                    "parent:" + parent + " child:" + child, ex);
        }

    }

    @Override
    public Id getGroupId(String groupName) {
        try {
            return personManagementServiceDao.getGroupId(groupName);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "getGroupId",
                    "groupName:" + groupName, ex);
        }
    }

    @Override
    public DomainObject findDynamicGroup(String name, Id contectId) {
        try {
            return personManagementServiceDao.findDynamicGroup(name, contectId);
        } catch (AccessException ex){
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new UnexpectedException("PersonManagementService", "findDynamicGroup",
                    "name:" + name + " contectId:" + contectId, ex);
        }
    }

}
