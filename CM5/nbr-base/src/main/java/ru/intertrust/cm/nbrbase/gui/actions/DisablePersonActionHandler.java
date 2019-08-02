package ru.intertrust.cm.nbrbase.gui.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentName("disable.person.handler")
public class DisablePersonActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired
    CrudService crudService;

    @Autowired
    ConfigurationControlService configurationControlService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    protected static Logger log = LoggerFactory.getLogger(DisablePersonActionHandler.class);

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        String result = "";
        final Id currentUserId = this.currentUserAccessor.getCurrentUserId();

        List<Id> objectsIds = context.getObjectsIds();
        if (objectsIds.isEmpty()) {
            objectsIds.add(context.getRootObjectId());
        }
        if (objectsIds.isEmpty()) {
            throw new GuiException("Не выбрано ни одного объекта");
        }

        List<DomainObject> persons = this.crudService.find(objectsIds);
        int i = 0;
        for (DomainObject person : persons) {
            if(person.getId().equals(currentUserId)){
                result="Удаление невозможно: Вы не можете удалить текущего пользователя.";
            }
            else{
                List<Id> groups = crudService.findLinkedDomainObjectsIds(person.getId(),"group_member","person_id");
                if(groups!=null){
                    crudService.delete(groups);
                }
                Map<String,Value> map = new HashMap<>();
                map.put("user_uid", new StringValue(person.getString("login")));
                DomainObject authInfo = crudService.findByUniqueKey("authentication_info",map);
                if (authInfo!=null){
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

        SimpleActionData aData = new SimpleActionData();
        if(context.getRootObjectId()!=null ||
                (context.getObjectsIds()!=null && context.getObjectsIds().size()>0)){
            aData.setSavedMainObjectId(context.getRootObjectId());
            List<Id> extIds = (context.getObjectsIds().size()>0)?context.getObjectsIds():
                    Arrays.asList(context.getRootObjectId());
            if(extIds!=null) {
                try {
                    configurationControlService.deactivateExtensionsById(extIds);
                    aData.setOnSuccessMessage("Объект успешно деактивирован.");
                } catch(ConfigurationException e){
                    throw new GuiException(e.getMessage());
                }

            }
        }
        return aData;
    }

}
