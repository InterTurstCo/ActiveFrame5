package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementService;

/**
 * Реализация сервиса вхождения управления пользователями и группами
 * 
 * @author larin
 * 
 */
public class PersonManagementServiceImpl implements PersonManagementService {

	@Autowired
	private CollectionsDao collectionsDao;

	@Autowired
	private DomainObjectDao domainObjectDao;

	@Autowired
	private AccessControlService accessControlService;

	/**
	 * Получение идентификатора персоны по логину
	 */
	@Override
	public Id getPersonId(String login) {
		Filter filter = new Filter();
		filter.setFilter("byLogin");
		StringValue sv = new StringValue(login);
		filter.addCriterion(0, sv);
		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		AccessToken accessToken = accessControlService
				.createSystemAccessToken("PersonManagementService");

		IdentifiableObjectCollection collection = collectionsDao
				.findCollection("PersonByLogin", filters, null, 0, 0,
						accessToken);
		Id result = null;
		if (collection.size() > 0) {
			IdentifiableObject io = collection.get(0);
			result = io.getId();
		}
		return result;
	}

	/**
	 * Получение персон входящих непосредственно в группу
	 */
	@Override
	public List<DomainObject> getPersonsInGroup(Id groupId) {
		Filter filter = new Filter();
		filter.setFilter("byGroup");
		ReferenceValue rv = new ReferenceValue(groupId);
		filter.addCriterion(0, rv);
		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		AccessToken accessToken = accessControlService
				.createSystemAccessToken("PersonManagementService");

		IdentifiableObjectCollection collection = collectionsDao
				.findCollection("PersonInGroup", filters, null, 0, 0,
						accessToken);
		List<DomainObject> result = new ArrayList<DomainObject>();
		for (IdentifiableObject item : collection) {
			DomainObject group = domainObjectDao
					.find(item.getId(), accessToken);
			result.add(group);
		}
		return result;
	}

	/**
	 * Получение всех персон входящих в группу с учетом наследования
	 */
	@Override
	public List<DomainObject> getAllPersonsInGroup(Id groupId) {
		Filter filter = new Filter();
		filter.setFilter("byGroup");
		ReferenceValue rv = new ReferenceValue(groupId);
		filter.addCriterion(0, rv);
		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		AccessToken accessToken = accessControlService
				.createSystemAccessToken("PersonManagementService");

		IdentifiableObjectCollection collection = collectionsDao
				.findCollection("AllPersonInGroup", filters, null, 0, 0,
						accessToken);
		List<DomainObject> result = new ArrayList<DomainObject>();
		for (IdentifiableObject item : collection) {
			DomainObject group = domainObjectDao
					.find(item.getId(), accessToken);
			result.add(group);
		}
		return result;
	}

	/**
	 * Проверка входит ли персона в группу
	 */
	@Override
	public boolean isPersonInGroup(Id groupId, Id personId) {
		Filter filter = new Filter();
		filter.setFilter("byGroupAndPerson");
		ReferenceValue rvGroup = new ReferenceValue(groupId);
		filter.addCriterion(0, rvGroup);
		ReferenceValue rvPerson = new ReferenceValue(personId);
		filter.addCriterion(1, rvPerson);
		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		AccessToken accessToken = accessControlService
				.createSystemAccessToken("PersonManagementService");

		IdentifiableObjectCollection collection = collectionsDao
				.findCollection("IsPersonInGroup", filters, null, 0, 0,
						accessToken);
		return collection.size() > 0;
	}

	/**
	 * Получение всех групп, куда входит персона
	 */
	@Override
	public List<DomainObject> getPersonGroups(Id personId) {
		Filter filter = new Filter();
		filter.setFilter("byPerson");
		ReferenceValue rv = new ReferenceValue(personId);
		filter.addCriterion(0, rv);
		List<Filter> filters = new ArrayList<>();
		filters.add(filter);

		AccessToken accessToken = accessControlService
				.createSystemAccessToken("PersonManagementService");

		IdentifiableObjectCollection collection = collectionsDao
				.findCollection("PersonGroups", filters, null, 0, 0,
						accessToken);
		List<DomainObject> result = new ArrayList<DomainObject>();
		for (IdentifiableObject item : collection) {
			DomainObject group = domainObjectDao
					.find(item.getId(), accessToken);
			result.add(group);
		}
		return result;
	}


}
