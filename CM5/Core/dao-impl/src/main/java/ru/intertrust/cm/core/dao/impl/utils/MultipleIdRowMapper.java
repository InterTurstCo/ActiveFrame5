package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
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
        while (rs.next()) {
            Id id = readId(rs, idField);
            if (id == null) {
                throw new FatalException("Id field can not be null for object " + domainObjectType);
            }
            ids.add(id);
        }
        return ids;
    }
}
