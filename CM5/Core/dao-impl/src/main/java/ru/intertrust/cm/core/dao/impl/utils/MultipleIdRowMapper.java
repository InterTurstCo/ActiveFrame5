package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Отображает {@link java.sql.ResultSet} на список идентификаторов
 * доменных объектов {@link java.util.List < ru.intertrust.cm.core.business.api.dto.Id >}.
 */
public class MultipleIdRowMapper implements ResultSetExtractor<List<Id>> {

    private Integer domainObjectTypeId;

    public MultipleIdRowMapper(Integer domainObjectTypeId) {
        this.domainObjectTypeId = domainObjectTypeId;
    }

    @Override
    public List<Id> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Id> ids = new ArrayList<>();
        while (rs.next()) {
            long id = rs.getLong(DefaultFields.DEFAULT_ID_FIELD);
            ids.add(new RdbmsId(domainObjectTypeId, id));
        }
        return ids;
    }
}
