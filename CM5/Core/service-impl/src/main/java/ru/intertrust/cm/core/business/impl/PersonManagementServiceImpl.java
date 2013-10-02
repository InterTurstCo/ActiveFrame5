package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;

@Stateless(name = "PersonManagementService")
@Local(PersonManagementService.class)
@Remote(PersonManagementService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PersonManagementServiceImpl implements PersonManagementService {

    @Autowired
    private PersonManagementServiceDao personManagementServiceDao;

    @Override
    public Id getPersonId(String login) {
        return personManagementServiceDao.getPersonId(login);
    }

    @Override
    public List<DomainObject> getPersonsInGroup(Id groupId) {
        return personManagementServiceDao.getPersonsInGroup(groupId);
    }

    @Override
    public List<DomainObject> getAllPersonsInGroup(Id groupId) {
        return personManagementServiceDao.getAllPersonsInGroup(groupId);
    }

    @Override
    public boolean isPersonInGroup(Id groupId, Id personId) {
        return personManagementServiceDao.isPersonInGroup(groupId, personId);
    }

    @Override
    public List<DomainObject> getPersonGroups(Id personId) {
        return personManagementServiceDao.getPersonGroups(personId);
    }

    @Override
    public boolean isGroupInGroup(Id parent, Id child, boolean recursive) {
        return personManagementServiceDao.isGroupInGroup(parent, child, recursive);
    }

    @Override
    public List<DomainObject> getAllParentGroup(Id child) {
        return personManagementServiceDao.getAllParentGroup(child);
    }

    @Override
    public List<DomainObject> getChildGroups(Id parent) {
        return personManagementServiceDao.getChildGroups(parent);
    }

    @Override
    public List<DomainObject> getAllChildGroups(Id parent) {
        return personManagementServiceDao.getAllChildGroups(parent);
    }

    @Override
    public void addPersonToGroup(Id group, Id person) {
        personManagementServiceDao.addPersonToGroup(group, person);
    }

    @Override
    public void addGroupToGroup(Id parent, Id child) {
        personManagementServiceDao.addGroupToGroup(parent, child);    
    }

    @Override
    public void remotePersonFromGroup(Id group, Id person) {
        personManagementServiceDao.remotePersonFromGroup(group, person);
    }

    @Override
    public void remoteGroupFromGroup(Id parent, Id child) {
        personManagementServiceDao.remoteGroupFromGroup(parent, child);
    }

    @Override
    public Id getGroupId(String groupName) {
        return personManagementServiceDao.getGroupId(groupName);
    }

}
