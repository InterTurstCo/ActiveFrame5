package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;

/**
 * DAO для работы с бизнесс объектами
 *
 */
public interface CrudServiceDAO {


    /**
     *
     * @param businessObjectConfig
     * @return
     */
	public long generateNextSequence(BusinessObjectConfig businessObjectConfig);

	public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);

	public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);

	public BusinessObject read(BusinessObject businessObjec, BusinessObjectConfig businessObjectConfig);

	public void delete(Id id, BusinessObjectConfig businessObjectConfig) throws ObjectNotFoundException;

	public boolean exists(Id id, BusinessObjectConfig businessObjectConfig);

	IdentifiableObjectCollection findCollectionByQuery(String query, String objectType, String idField, int offset, int limit);


}
