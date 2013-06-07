package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.config.BusinessObjectConfig;

/**
 * DAO для работы с бизнесс объектами
 *  
 */
public interface CrudServiceDAO {
	
	
	public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);
	
	public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);
	
	public BusinessObject read(BusinessObject businessObjec, BusinessObjectConfig businessObjectConfig);
	
	
	

}
