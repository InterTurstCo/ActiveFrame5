package ru.intertrust.cm.core.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
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
     * Получение коллекции объектов по имени коллекции в collections.xml
     * @param collectionName
     * @return
     */
    public List<Id>  find(String collectionName) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");

        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, null, null, 0, 0, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return collectionToList(collection);
    }

    /**
     * Выполнение запроса и возвращение коллекции
     * @param query
     * @param params
     * @return
     */
    public IdentifiableObjectCollection findByQuery(String query, Object ... params) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken(getClass().getName());
        List<Value> queryParams = new ArrayList<Value>();
        for (Object param : params) {
            if (param instanceof Id){
                queryParams.add(new ReferenceValue((Id)param));
            }else if(param instanceof String){
                queryParams.add(new StringValue((String)param));
            }else if(param instanceof Integer){
                queryParams.add(new LongValue((Integer)param));
            }else if(param instanceof Long){
                queryParams.add(new LongValue((Long)param));
            }else if(param instanceof Boolean){
                queryParams.add(new BooleanValue((Boolean)param));
            }
        }
        IdentifiableObjectCollection collection = getCollectionService().findCollectionByQuery(query, queryParams, 0, 0, accessToken);
        return collection;
    }
    
    
    /**
     * Получение коллекции c фильтром по id. Фильтр должен быть по id
     * @param collectionName
     * @param id - id объекта
     * @param filterName - имя фильтра
     * @return
     * @deprecated испольуйте {@link find(String collectionName,ScriptTaskFilter filters)} .  
     */
    @Deprecated
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
        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, filters, null, 0, 0, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return collection;
    }
    /**
     * Получение коллекции c фильтрацией
     * @param collectionName
     * @param o - массив параметров
     * @return
     */
    public List<Id> find(String collectionName,ScriptTaskFilter filters) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");
        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, filters.getFiltersList(), null, 0, 0, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return collectionToList(collection);
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
     * @param collection - коллекция Id
     * @param status - новый статус
     */
    public void setCardsStatus(ArrayList<Id> collection, String status){
    	Iterator<Id> iter = collection.iterator();
    	while (iter.hasNext()){
    		DomainObjectAccessor card = find(iter.next());
    		card.setStatus(status);
    	}
    }
    
    /**
     * Изменить статус для всех карточек в коллекции, которые имеют определенный статус
     * @param collection - коллекция Id
     * @param fromStatus - текущий статус карточки
     * @param toStatus - новый статус
     * @param allButOne - флаг. Если true, то поменять статус у всех карточек, статус которых не равен <fromStatus>. Если false, то поменять статус только у карточек со статусом <fromStatus>.
     */
    public void setCardsStatus(ArrayList<Id> collection, String fromStatus, String toStatus, Boolean allButOne){
    	Iterator<Id> iter = collection.iterator();
    	while (iter.hasNext()){
    		DomainObjectAccessor card = find(iter.next());
    		//Попадаем в условие, когда только 1 из аргументов=true
    		if (allButOne^card.getStatus().equals(fromStatus)){
    			card.setStatus(toStatus);
    		}   		
    	}
    }
    
    /**
     * Создание фильтра
     * @return
     */
    public ScriptTaskFilter createFilter(){
    	return new ScriptTaskFilter();
    }
    
    /**
     * Возвращает строковое представление id
     * @param id
     * @return
     */
    public String getStrId(Id id){
    	return id.toStringRepresentation();
    }
    
    /**
     * Преобразование коллекции в список для использование в JavaScript
     * @param collection
     * @return
     */
    private List<Id> collectionToList(IdentifiableObjectCollection collection){
        Iterator<IdentifiableObject> iterator2 = collection.iterator();
        ArrayList<Id> list = new ArrayList<Id>();
        while (iterator2.hasNext()){
        	list.add(iterator2.next().getId());
        }
        return list;
    }
    
    public NotificationAddresseeConverter getNotificationAddressee(){
        return new NotificationAddresseeConverter();
    }
    
}
