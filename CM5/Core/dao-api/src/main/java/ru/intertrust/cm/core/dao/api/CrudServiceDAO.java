package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.BusinessObjectConfig;

/**
 * DAO для работы с бизнесс объектами
 *
 */
public interface CrudServiceDAO {


	public int generateNextSequence();

	public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);

	public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig);

	public BusinessObject read(BusinessObject businessObjec, BusinessObjectConfig businessObjectConfig);

	public void delete(Id id, BusinessObjectConfig businessObjectConfig);

	public boolean exists(Id id, BusinessObjectConfig businessObjectConfig);

	IdentifiableObjectCollection findCollectionByQuery(String query, String objectType, String idField, int offset, int limit);


}
