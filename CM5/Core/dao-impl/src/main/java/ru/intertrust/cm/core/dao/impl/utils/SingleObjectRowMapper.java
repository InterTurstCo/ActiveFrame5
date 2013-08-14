package ru.intertrust.cm.core.dao.impl.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;


/**
 * Отображает {@link java.sql.ResultSet} на доменный объект {@link ru.intertrust.cm.core.business.api.dto.DomainObject}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class SingleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<DomainObject> {

    public SingleObjectRowMapper(String domainObjectType, ConfigurationExplorer configurationExplorer) {
        super(domainObjectType, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer);
    }

    @Override
    public DomainObject extractData(ResultSet rs) throws SQLException, DataAccessException {
        GenericDomainObject object = null;

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumnNames().add(fieldName);
        }

        while (rs.next()) {
            object = new GenericDomainObject();
            object.setTypeName(domainObjectType);

            for (String columnName : columnModel.getColumnNames()) {
                FieldValueModel valueModel = new FieldValueModel();
                fillValueModel(rs, valueModel, columnName);
                fillObjectValue(object, valueModel, columnName);
            }
        }
        return object;
    }    

}
