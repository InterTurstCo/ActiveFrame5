package ru.intertrust.cm.test.acess.dynamicgroup;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.CollectorSettings;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.DynamicGroupCollector;

/**
 * TODO Коментарии Ларина М.
 * 1. Класс реализует логику замещения между штатными единицами. Надо дореализовывать если замещаются роли или организации или подразделения
 * 2. Некорректно делать одну роль для аудиторов и заместителей, так как им раздаются разные права. Нкжно делать две роли
 *
 */
public class PersonAssistantAuditor extends DynamicGroupCollectorBase implements DynamicGroupCollector {

    @Override
    public List<Id> getPersons(Id contextId) {
        AccessToken accessToken = accessControlService
                .createSystemAccessToken(this.getClass().getName());

        List<Id> result = new ArrayList<Id>();
        //Приходит идентификатор бороды, получаем назначение
        Id appointmentId = getIdByQuery("select t.id from SO_Appointment t where t.Beard = " + ((RdbmsId) contextId).getId());
        DomainObject appointment = domainObjectDao.find(appointmentId, accessToken);

        //По назначению ищем персону
        Id personSysId = appointment.getReference("Person");
        DomainObject personSys = domainObjectDao.find(personSysId, accessToken);
        Id personId = personSys.getReference("PlatformPerson");

        //Добавляем персону в результат
        result.add(personId);

        //Получаем штатную еденицу
        Id postId = appointment.getReference("Post");

        //Ищем всех замов
        result.addAll(getSubstitute(postId));

        //Ищем всех аудиторов
        result.addAll(getAuditor(postId));

        return result;
    }

    /**
     * Получение пользователей штатных единиц заместителей
     * @param postId
     */
    private List<Id> getSubstitute(Id postId) {
        List<Id> result =
                getIdsByQuery(
                        "select ps.PlatformPerson from SO_Substitute_Unit su inner join SO_AppointmentPlan ap on (ap.id = su.Substitute) inner join SO_PersonSys ps on (ps.id = ap.Person) where su.Substituted = "
                                + ((RdbmsId) postId).getId(), "PlatformPerson");
        return result;
    }

    /**
     * Получение пользователей штатных единиц аудиторов
     * @param postId
     */
    private List<Id> getAuditor(Id postId) {
        List<Id> result =
                getIdsByQuery(
                        "select ps.PlatformPerson from SO_Auditor_Unit au inner join SO_AppointmentPlan ap on (ap.id = au.Auditor) inner join SO_PersonSys ps on (ps.id = ap.Person) where su.Audited = "
                                + ((RdbmsId) postId).getId(), "PlatformPerson");
        return result;
    }

    @Override
    public List<Id> getGroups(Id contextId) {
        return null;
    }

    @Override
    public List<String> getTrackTypeNames() {
        List<String> result = new ArrayList<String>();
        result.add("SO_Appointment");
        result.add("SO_Substitute");
        result.add("SO_Auditor");
        return result;
    }

    @Override
    public List<Id> getInvalidContexts(DomainObject domainObject, List<FieldModification> modifiedFields) {
        //Получение бороды, которая изменится исходя из изменяемого объекта
        List<Id> result = null;
        if (domainObject.getTypeName().equals("SO_Appointment")) {
            //Получение бороды по назначению
            result = getIdsByQuery("select t.Beard from SO_Appointment t where t.id = " + ((RdbmsId) domainObject.getId()).getId(), "Beard");
        } else if (domainObject.getTypeName().equals("SO_Substitute")) {
            //Получение бороды по замещению
            result =
                    getIdsByQuery("select ap.Beard from SO_AppointmentPlan ap inner join SO_PostPlain pp on (pp.id = ap.Post) inner join SO_Substitute_Unit su on (su.Substituted = pp.id) where su.id = "
                            + ((RdbmsId) domainObject.getId()).getId());
        } else if (domainObject.getTypeName().equals("SO_Auditor")) {
            //Получение бороды по аудиту
            result =
                    getIdsByQuery("select ap.Beard from SO_AppointmentPlan ap inner join SO_PostPlain pp on (pp.id = ap.Post) inner join SO_Auditor_Unit au on (au.Audited = pp.id) where au.id = "
                            + ((RdbmsId) domainObject.getId()).getId());
        }
        return result;
    }

    @Override
    public void init(DynamicGroupConfig config, CollectorSettings setings) {
    }

}
