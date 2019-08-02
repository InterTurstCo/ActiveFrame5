package ru.intertrust.cm.nbrbase.gui.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentName("person.delete.handler")
public class PersonDeleteInterceptor implements FormObjectsRemoverExtStr {

    @Autowired
    CrudService crudService;

    @Autowired
    ConfigurationControlService configurationControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    protected CurrentUserAccessor currentUserAccessor;


    @Override
    public void deleteForm(FormState currentFormState) {
        this.deleteObj(Arrays.asList(currentFormState.getObjects().getRootDomainObject().getId()));
    }

    @Override
    public String deleteObj(List<Id> objectsIds){
        int i = deleteObjects(objectsIds);
        if(i>1) {
            if(conflictMessage.length()!=0){
                return "Успешно удалено " + i + " объектов из " + objectsIds.size() + conflictMessage;
            }
            return "Успешно удалено " + i + " объектов из " + objectsIds.size();
        }
        if(conflictMessage.length()!=0){
            return conflictMessage;
        }
        return "Обьект успешно удален";
    }
    private String conflictMessage="";
    @Override
    public int deleteObjects(List<Id> objectsIds) {
        final Id currentUserId = this.currentUserAccessor.getCurrentUserId();
        String result = "";
        List<DomainObject> persons = this.crudService.find(objectsIds);
        int i = 0;
        for (DomainObject person : persons) {
            if (person.getId().equals(currentUserId)) {
                throw new GuiException("Удаление невозможно: Вы не можете удалить текущего пользователя.");
            } else {
                List<Id> groups = crudService.findLinkedDomainObjectsIds(person.getId(), "group_member", "person_id");
                if (groups != null) {
                    crudService.delete(groups);
                }
                Map<String, Value> map = new HashMap<>();
                map.put("user_uid", new StringValue(person.getString("login")));
                DomainObject authInfo = crudService.findByUniqueKey("authentication_info", map);
                if (authInfo != null) {
                    crudService.delete(authInfo.getId());
                }

                map = new HashMap<>();
                map.put("name", new StringValue("Inactive"));
                DomainObject statusSleep = crudService.findByUniqueKey("status", map);

                if (accessControlService != null && domainObjectDao != null) {
                    AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
                    person = domainObjectDao.setStatus(person.getId(), statusSleep.getId(), accessToken);
                    domainObjectDao.save(person, accessToken);
                } else {
                    throw new GuiException("Ошибка инициализации объектов доступа");
                }
                i++;
            }
        }
        return i;
    }

}
