package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.dao.impl.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Отображает {@link java.sql.ResultSet} на список доменных объектов {@link java.util.List < ru.intertrust.cm.core.business.api.dto.DomainObject >}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class MultipleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<List<DomainObject>> {

    private static final String DEFAULT_ID_FIELD = "id";

    public MultipleObjectRowMapper(String domainObjectType) {
        super(domainObjectType, DEFAULT_ID_FIELD);
    }

    @Override
    public List<DomainObject> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<DomainObject> objects = new ArrayList<>();

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
            GenericDomainObject object = new GenericDomainObject();
            FieldValueModel valueModel = new FieldValueModel();

            object.setTypeName(domainObjectType);
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
            objects.add(object);
        }

        return objects;
    }
}
