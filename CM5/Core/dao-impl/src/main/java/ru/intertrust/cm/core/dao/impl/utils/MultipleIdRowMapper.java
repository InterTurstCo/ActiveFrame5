package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.impl.ResultSetExtractionLogger;
import ru.intertrust.cm.core.model.FatalException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Отображает {@link java.sql.ResultSet} на список идентификаторов
 * доменных объектов {@link java.util.List < ru.intertrust.cm.core.business.api.dto.Id >}.
 */
public class MultipleIdRowMapper extends BasicRowMapper implements ResultSetExtractor<List<Id>> {

    public MultipleIdRowMapper(String domainObjectType) {
        super(domainObjectType, DefaultFields.DEFAULT_ID_FIELD, null, null);
    }

    @Override
    public List<Id> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Id> ids = new ArrayList<>();
        long rowCount = 0;
        final long start = System.currentTimeMillis();
        while (rs.next()) {
            ResultSetExtractionLogger.log("MultipleIdRowMapper.extractData", start, ++rowCount);
            Id id = readId(rs, idField);
            if (id == null) {
                throw new FatalException("Id field can not be null for object " + domainObjectType);
            }
            ids.add(id);
        }
        return ids;
    }
}
