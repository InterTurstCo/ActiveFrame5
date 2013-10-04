package ru.intertrust.cm.core.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public class Session implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4445325913528199819L;

    /**
     * Создание нового доменного объекта переданного типа
     * @param type
     * @return
     */
    public DomainObjectAccessor create(String type) {
        DomainObject domainObject = createDomainObject(type);
        return new DomainObjectAccessor(domainObject);
    }

    /**
     * Получение коллекции
     * @param collectionName
     * @return
     */
    public List<DomainObjectAccessor> find(String collectionName) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");

        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, null, null, 0, 1000, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return result;
    }

    /**
     * Создание нового доменного обьекта переданного типа
     * 
     * @param type
     * @return
     */
    private DomainObject createDomainObject(String type) {
        GenericDomainObject taskDomainObject = new GenericDomainObject();
        taskDomainObject.setTypeName(type);
        Date currentDate = new Date();
        taskDomainObject.setCreatedDate(currentDate);
        taskDomainObject.setModifiedDate(currentDate);
        return taskDomainObject;
    }

    /**
     * Получение сервиса коллекций
     * @return
     */
    private CollectionsDao getCollectionService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(CollectionsDao.class);
    }

    /**
     * Получение сервиса безопасности
     * @return
     */
    private AccessControlService getAccessControlService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(AccessControlService.class);
    }

}
