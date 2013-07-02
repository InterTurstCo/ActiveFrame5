package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.dao.impl.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Отображает {@link java.sql.ResultSet} на {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class CollectionRowMapper extends BasicRowMapper implements
        ResultSetExtractor<IdentifiableObjectCollection> {

    public CollectionRowMapper(String domainObjectType, String idField) {
        super(domainObjectType, idField);
    }

    @Override
    public IdentifiableObjectCollection extractData(ResultSet rs) throws SQLException, DataAccessException {
        IdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            DataType fieldType = getColumnType(rs.getMetaData().getColumnTypeName(i));
            if (fieldName.equals(idField)) {
                columnModel.setIdField(fieldName);
                columnModel.getColumnTypes().add(DataType.ID);
            } else {
                columnModel.getColumnNames().add(fieldName);
                columnModel.getColumnTypes().add(fieldType);
            }

        }

        collection.setFields(columnModel.getColumnNames());

        int row = 0;
        while (rs.next()) {
            FieldValueModel valueModel = new FieldValueModel();

            int index = 0;
            int collectionIndex = 0;

            for (DataType fieldType : columnModel.getColumnTypes()) {
                fillValueModel(valueModel, rs, columnModel, index, fieldType);

                collectionIndex = index;

                if (valueModel.getId() != null) {
                    collection.setId(row, valueModel.getId());
                    collectionIndex = index == 0 ? 0 : index - 1;
                }
                if (valueModel.getValue() != null) {
                    collection.set(collectionIndex, row, valueModel.getValue());
                }
                index++;
            }

            row++;
        }
        return collection;
    }
}
