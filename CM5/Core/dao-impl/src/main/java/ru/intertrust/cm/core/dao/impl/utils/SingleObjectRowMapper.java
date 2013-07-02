package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.dao.impl.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Отображает {@link java.sql.ResultSet} на доменный объект {@link ru.intertrust.cm.core.business.api.dto.DomainObject}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class SingleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<DomainObject> {

    private static final String DEFAULT_ID_FIELD = "id";

    public SingleObjectRowMapper(String domainObjectType) {
        super(domainObjectType, DEFAULT_ID_FIELD);
    }

    @Override
    public DomainObject extractData(ResultSet rs) throws SQLException, DataAccessException {
        GenericDomainObject object = new GenericDomainObject();
        object.setTypeName(domainObjectType);

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            DataType fieldType = getColumnType(rs.getMetaData().getColumnTypeName(i));
            if (fieldName.equalsIgnoreCase(idField)) {
                columnModel.setIdField(fieldName);
                columnModel.getColumnTypes().add(DataType.ID);
            } else {
                columnModel.getColumnNames().add(fieldName);
                columnModel.getColumnTypes().add(fieldType);
            }

        }

        while (rs.next()) {
            FieldValueModel valueModel = new FieldValueModel();
            int index = 0;
            int fieldIndex = 0;
            for (DataType fieldType : columnModel.getColumnTypes()) {

                fillValueModel(valueModel, rs, columnModel, index, fieldType);

                fieldIndex = index;

                if (valueModel.getId() != null) {
                    object.setId(valueModel.getId());
                    fieldIndex = index == 0 ? 0 : index - 1;
                }
                if (valueModel.getValue() != null) {
                    String columnName = columnModel.getColumnNames().get(fieldIndex);
                    object.setValue(columnName, valueModel.getValue());

                }
                index++;
            }

        }
        return object;
    }

}
