package ru.intertrust.cm.core.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
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
    public IdentifiableObjectCollection find(String collectionName) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");

        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, null, null, 0, 1000, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return collection;
    }
    
    /**
     * Получение коллекции c фильтром по id. Фильтр должен быть по id
     * @param collectionName
     * @param id - id объекта
     * @param filterName - имя фильтра
     * @return
     */
    public IdentifiableObjectCollection find(String collectionName, Id id, String filterName) {

        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");
        //Создаем новый фильтр
        Filter filter = new Filter();
        filter.setFilter(filterName);
        //Создаем критерий фильтра
        ReferenceValue lv = new ReferenceValue(id);
        filter.addCriterion(0, lv);
        //Создаем массив фильтров
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);
        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, filters, null, 0, 1000, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return collection;
    }
    /**
     * Получение коллекции c фильтром по id. Фильтр должен быть по id
     * @param collectionName
     * @param o - массив параметров
     * @return
     */
    public List<IdentifiableObject> find(String collectionName,ScriptTaskFilter filters) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");
        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, filters.getFiltersList(), null, 0, 1000, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        Iterator<IdentifiableObject> iterator2 = collection.iterator();
        ArrayList<IdentifiableObject> list = new ArrayList<IdentifiableObject>();
        while (iterator2.hasNext()){
        	list.add(iterator2.next());
        }
        return list;
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
    
    /**
     * Получение сервиса IdService
     * @return
     */
    private IdService getIdService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(IdService.class);
    }
    
    /**
     * Получение сервиса CrudService
     * @return
     */
    private CrudService getCrudService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(CrudService.class);
    }
    
    /**
     * Получение объекта по Id
     */
    public DomainObjectAccessor find(Id id){
    	return new DomainObjectAccessor(id);
    }
    
    /**
     * Получение объекта по StringRepresentation Id
     */
    public DomainObjectAccessor findByStrId(String strId){
    	return new DomainObjectAccessor(getIdService().createId(strId));
    }
    
    /**
     * Выводв лог информации
     * @param msg - сообщение
     */
    public void Log(String msg){
        System.out.println(msg);
    }
    /**
     * Изменить статус для всех карточек в коллекции
     * @param collection - коллекция IdentifiableObject
     * @param status - новый статус
     */
    public void setCardsStatus(ArrayList<IdentifiableObject> collection, String status){
    	Iterator<IdentifiableObject> iter = collection.iterator();
    	while (iter.hasNext()){
    		DomainObjectAccessor card = find(iter.next().getId());
    		card.setStatus(status);
    	}
    }
    
    /**
     * Изменить статус для всех карточек в коллекции, которые имеют определенный статус
     * @param collection - коллекция IdentifiableObject
     * @param fromStatus - текущий статус карточки
     * @param toStatus - новый статус
     * @param allButOne - флаг. Если true, то поменять статус у всех карточек, статус которых не равен <fromStatus>. Если false, то поменять статус только у карточек со статусом <fromStatus>.
     */
    public void setCardsStatus(ArrayList<IdentifiableObject> collection, String fromStatus, String toStatus, Boolean allButOne){
    	Iterator<IdentifiableObject> iter = collection.iterator();
    	while (iter.hasNext()){
    		DomainObjectAccessor card = find(iter.next().getId());
    		if (allButOne^card.getStatus().equals(fromStatus)){
    			card.setStatus(toStatus);
    		}   		
    	}
    }
    public ScriptTaskFilter createFilter(){
    	return new ScriptTaskFilter();
    }

}
