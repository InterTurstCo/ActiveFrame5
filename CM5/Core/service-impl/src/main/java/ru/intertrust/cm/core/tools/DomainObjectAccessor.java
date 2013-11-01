package ru.intertrust.cm.core.tools;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.model.DecimalFieldConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.LongFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Класс обертка над сервисом domainObjectDao для более удобной работы с объектм
 * Внутри класса нельзя использовать @Autowired, так как обязательно обьект
 * должен серелиазоватся
 * 
 * @author larin
 * 
 */
public class DomainObjectAccessor implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = -8436920175908035925L;
    private DomainObject domainObject = null;

    /**
     * Конструктор. Инициализирует доменный обьект
     * 
     * @param id
     */
    public DomainObjectAccessor(Id id) {
        load(id);
    }

    private void load(Id id){
        // TODO как сделается нормальная работа с токенами заменить токен на
        // токен для конкретного пользователя
        AccessToken accessToken = getAccessControlService()
                .createSystemAccessToken("DomainObjectAccessor");
        domainObject = getDomainObjectDao().find(id, accessToken);        
    }
    
    public DomainObjectAccessor(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    /**
     * Получает поле объекта
     * 
     * @param fieldName - имя поля. Может быть составным Negotiator.Employee.Name - вернет значение поля "Name" объекта Employee, ссылка на который хранится в поле Negotiator объекта domainObject
     * @return
     */
    public Object get(String fieldName) {
    	Object result = null;
    	//Обновляем доменный объект
    	load(domainObject.getId());
    	
    	//Если fieldName - простое
    	if (fieldName.indexOf(".")==-1){
            Value value = domainObject.getValue(fieldName);
            if (value != null) {
                result = value.get();
            }
        //Если fieldName - составное (пример: Negotiator.Employee.Name)
    	}else{
    		//Получаем имя поля (пример: Negotiator)
    		String value = fieldName.substring(0,fieldName.indexOf("."));
    		//Получаем остальную часть строки (пример: Employee.Name)
    		String other = fieldName.substring(fieldName.indexOf(".")+1);
    		//Получаем дочерний объект по имени поля (пример: Negotiator)
    		DomainObjectAccessor doa = new DomainObjectAccessor(domainObject.getReference(value));
    		//Получаем у дочернего объекта значение поля (пример: Employee.Name) 
    		result = doa.get(other);
    	}

        return result;
    }

    /**
     * Устанавливает поле объекта
     * 
     * @param fieldName
     * @param value
     */
    public void set(String fieldName, Object value) {
        FieldConfig config = getConfigurationExplorer().getFieldConfig(domainObject.getTypeName(), fieldName);
        if (config instanceof LongFieldConfig) {
            if (value instanceof Long) {
                domainObject.setLong(fieldName, (long) value);
            } else if (value instanceof Double) {
                domainObject.setLong(fieldName, ((Double) value).longValue());
            } else {
                domainObject.setLong(fieldName, Long.parseLong(value.toString()));
            }
        } else if (config instanceof DecimalFieldConfig) {
            if (value instanceof BigDecimal) {
                domainObject.setDecimal(fieldName, (BigDecimal) value);
            } else {
                domainObject.setDecimal(fieldName, BigDecimal.valueOf((double) value));
            }
        } else if (config instanceof DateTimeFieldConfig) {
            if (value instanceof Date) {
                domainObject.setTimestamp(fieldName, (Date) value);
            } else {
                // Преобразовываем из других форматов
                // domainObject.setTimestamp(fieldName,
                // BigDecimal.valueOf((double)value));
            }
        } else if (config instanceof ReferenceFieldConfig) {
            if (value instanceof Id) {
                domainObject.setReference(fieldName, (Id) value);
            } else {
                // Преобразовываем из других форматов
                // domainObject.setReference(fieldName, value);
            }
        } else {
            domainObject.setString(fieldName, value.toString());
        }
    }

    /**
     * Сохраняет обьект
     */
    public void save() {
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("DomainObjectAccessor");
        domainObject = getDomainObjectDao().save(domainObject, accessToken);
    }
    
    
    /**
     * Получение сервиса DomainObjectDao
     * 
     * @return
     */
    private DomainObjectDao getDomainObjectDao() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(DomainObjectDao.class);
    }

    /**
     * Получение сервиса AccessControlService
     * 
     * @return
     */
    private AccessControlService getAccessControlService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(AccessControlService.class);
    }

    /**
     * Получение сервиса для работы с конфигурацией
     * @return
     */
    private ConfigurationExplorer getConfigurationExplorer() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return (ConfigurationExplorer) ctx.getBean("configurationExplorer");
    }
    
    /**
     * Установка статуса объекта
     * @param status
     */
    public void setStatus(String statusName){

    	CrudService crudService = getCrudService();
    	//Создание статуса черновик
        List<DomainObject> allStatuses = crudService.findAll("Status");
        Iterator<DomainObject> iter = allStatuses.iterator();
        DomainObject status = null;
        DomainObject newStatus = null;
       //TODO  Создать статусы заранее
        while (iter.hasNext()){
        	status = iter.next();
        	if ((status.getString("Name").equals(statusName))){
        		newStatus = status;
        	}
        }
        if (newStatus ==null){
        	newStatus = createStatus(statusName);
        }
        
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("DomainObjectAccessor");
        domainObject = getDomainObjectDao().setStatus(domainObject.getId(), newStatus.getId(), accessToken);
    }
    /**
     * Получить статус объекта
     * @return
     */
    public String getStatus(){
    	//TODO Разобрать почему в domainObject.getReference("status") ).getTypeId() не обновляется статус
    	
    	CrudService crudService = getCrudService();
    	DomainObject status = crudService.find(domainObject.getStatus());
    	return status.getString("Name");
    }
    
    /**
     * Получение сервиса CrudService
     * @return
     */
    private CrudService getCrudService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(CrudService.class);
    }
    
    private  DomainObject createStatus(String statusName) {
        GenericDomainObject statusDO = new GenericDomainObject();
        statusDO.setTypeName(GenericDomainObject.STATUS_DO);
        Date currentDate = new Date();
        statusDO.setCreatedDate(currentDate);
        statusDO.setModifiedDate(currentDate);
        statusDO.setString("Name", statusName);
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("InitialDataLoader");
        return getDomainObjectDao().save(statusDO, accessToken);
    }
    

    
    

}
