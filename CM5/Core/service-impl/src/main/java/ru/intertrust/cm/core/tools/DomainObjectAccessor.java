package ru.intertrust.cm.core.tools;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
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
	 * @param fieldName
	 * @return
	 */
	public Object get(String fieldName) {
		Value value = domainObject.getValue(fieldName);
		Object result = null;
		if (value != null) {
			result = value.get();
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
			if (value instanceof Long){
				domainObject.setLong(fieldName, (long)value);
			}else if(value instanceof Double){
				domainObject.setLong(fieldName, ((Double)value).longValue());
			}else{
				domainObject.setLong(fieldName, Long.parseLong(value.toString()));
			}
		} else if (config instanceof DecimalFieldConfig) {
			if (value instanceof BigDecimal){
				domainObject.setDecimal(fieldName, (BigDecimal)value);
			}else{
				domainObject.setDecimal(fieldName, BigDecimal.valueOf((double)value));
			}
		} else if (config instanceof DateTimeFieldConfig) {
			if (value instanceof Date){
				domainObject.setTimestamp(fieldName, (Date)value);
			}else{
				//Преобразовываем из других форматов
				//domainObject.setTimestamp(fieldName, BigDecimal.valueOf((double)value));
			}
		} else if (config instanceof ReferenceFieldConfig) {
			if (value instanceof Id){
				domainObject.setReference(fieldName, (Id) value);
			}else{
				//Преобразовываем из других форматов
				//domainObject.setReference(fieldName, value);
			}			
		} else {
			domainObject.setString(fieldName, value.toString());
		}
	}

	/**
	 * Сохраняет обьект
	 */
	public void save() {
		domainObject = getDomainObjectDao().save(domainObject);
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
	 * Создание нового доменного объекта переданного типа
	 * @param type
	 * @return
	 */
	public DomainObjectAccessor create(String type){
		DomainObject domainObject = createDomainObject(type);
		return new DomainObjectAccessor(domainObject);
	}

	public ConfigurationExplorer getConfigurationExplorer(){
		ApplicationContext ctx = SpringApplicationContext.getContext();
		return (ConfigurationExplorer)ctx.getBean("configurationExplorer");
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

}
