package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.ResultSetExtractionLogger;

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

    public MultipleObjectRowMapper(String domainObjectType, ConfigurationExplorer configurationExplorer,
                                   DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache);
    }

    @Override
    public List<DomainObject> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<DomainObject> objects = new ArrayList<>();
        ColumnModel columnModel = new ColumnModel();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String fieldName = rs.getMetaData().getColumnName(i);
            columnModel.getColumns().add(new Column(i, fieldName));
        }
        long rowCount = 0;
        final long start = System.currentTimeMillis();
        while (rs.next()) {
            ResultSetExtractionLogger.log("MultipleObjectRowMapper.extractData", start, ++rowCount);
            DomainObject object = buildDomainObject(rs, columnModel);
            objects.add(object);
        }
        return objects;
    }
}
