package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.api.IdGenerator;

/**
 * Создает(генерирует) уникальный идентификатор используя последовательность(сиквенс)  в базе данных
 * @author skashanski
 *
 */
public class SequenceIdGenerator implements IdGenerator {


    private CrudServiceDAO crudServiceDAO;


    public void setCrudServiceDAO(CrudServiceDAO crudServiceDAO) {
        this.crudServiceDAO = crudServiceDAO;
    }




    @Override
    public Object generatetId(BusinessObjectConfig businessObjectConfig) {

        String sequenceName = DataStructureNamingHelper.getSqlSequenceName(businessObjectConfig);


        return crudServiceDAO.generateNextSequence(sequenceName);


    }

}
