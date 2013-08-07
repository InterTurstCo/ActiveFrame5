package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;

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

    public MultipleObjectRowMapper(String domainObjectType, ConfigurationExplorer configurationExplorer) {
        super(domainObjectType, DEFAULT_ID_FIELD, configurationExplorer);
    }

    @Override
    public List<DomainObject> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<DomainObject> objects = new ArrayList<>();

        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumnNames().add(fieldName);
        }

        while (rs.next()) {
            GenericDomainObject object = new GenericDomainObject();
            object.setTypeName(domainObjectType);
            for (String columnName : columnModel.getColumnNames()) {
                FieldValueModel valueModel = new FieldValueModel();
                fillValueModel(rs, valueModel, columnName);
                fillObjectValue(object, valueModel, columnName);
            }

            objects.add(object);
        }

        return objects;
    }
}
