package ru.intertrust.cm.core.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.mozilla.javascript.NativeObject;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
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
    public IdentifiableObjectCollection find(String collectionName,Hashtable<String, Object> filtersHT) {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("Session");
        //Создаем массив фильтров
        List<Filter> filters = new ArrayList<>();
        //Получаем ассоциативный массив фильтров из JavaScript [key]->[value] 
        //key=Filter name
        //value=Filter criterian или массив criterian
        Set<Entry<String, Object>> entrySet = filtersHT.entrySet();

        Iterator<Entry<String, Object>> iterator = entrySet.iterator();
        while (iterator.hasNext()){
        	//Создаем новый фильтр
            Filter filter = new Filter();
            //Получаем имя фильтра
            Entry<String, Object> entry = iterator.next();
        	String key = (String) entry.getKey(); 
        	//Устанавливаем имя фильтра
            filter.setFilter(key);     
            //Получаем значение фильтра
        	Object value =  entry.getValue();
        	if (value instanceof Id){
                //Создаем критерий фильтра
                ReferenceValue rv = new ReferenceValue((Id)value);
                filter.addCriterion(0, rv);
        	}else if (value instanceof String){
                //Создаем критерий фильтра
                StringValue sv = new StringValue((String)value);
                filter.addCriterion(0, sv);
        	}else if (value instanceof Double){
                //Создаем критерий фильтра
                LongValue dv = new LongValue(((Double) value).longValue());
                filter.addCriterion(0, dv);
        	}else if (value instanceof List){
        		//TODO если приходит массив сделать разбор
        	}
           filters.add(filter);
        }
        IdentifiableObjectCollection collection = getCollectionService().findCollection(collectionName, filters, null, 0, 1000, accessToken);
        // TODO перобразовать коллекцию в коллекцию DomainObjectAccessor, но там
        // неи должно быть save, тоесть нужно создать другой класс для результата коллекции
        // List<DomainObjectAccessor> result = new ArrayList<DomainObjectAccessor>();
        return collection;
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

}
