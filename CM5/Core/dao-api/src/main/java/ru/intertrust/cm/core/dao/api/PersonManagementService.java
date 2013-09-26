package ru.intertrust.cm.core.dao.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис для более удобной работы с пользователями и группами
 * @author larin
 *
 */
public interface PersonManagementService {
	/**
	 * Получение идентификатора персоны по его логину
	 * @param login
	 * @return
	 */
	Id getPersonId(String login);

	/**
	 * Получение списка персон, входящих в группу
	 * @param groupId
	 * @return
	 */
	List<DomainObject> getPersonsInGroup(Id groupId);

	/**
	 * Получение списка персон входящих в группу, с учетом вхождения группы в группу
	 * @param groupId
	 * @return
	 */
	List<DomainObject> getAllPersonsInGroup(Id groupId);
	
	/**
	 * Проверка входит ли персона в группу
	 * @param groupId
	 * @return
	 */
	boolean isPersonInGroup(Id groupId, Id personId);
	
	/**
	 * Получения списка групп, в которые входит персона с учетом наследования
	 * @param personId
	 * @return
	 */
	List<DomainObject> getPersonGroups(Id personId);
	
}
