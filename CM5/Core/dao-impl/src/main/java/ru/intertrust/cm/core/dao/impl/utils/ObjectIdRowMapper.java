package ru.intertrust.cm.core.dao.impl.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;

/**
 * Отображает {@link java.sql.ResultSet} на идентификатор доменного объекта {@link Id}
 * @author atsvetkov
 */
public class ObjectIdRowMapper implements ResultSetExtractor<Id> {

    private String idField;

    private Integer domainObjectType;

    public ObjectIdRowMapper(String idField, Integer domainObjectType) {
        this.idField = idField;
        this.domainObjectType = domainObjectType;
    }

    @Override
    public Id extractData(ResultSet rs) throws SQLException, DataAccessException {
        Id id = null;
        while (rs.next()) {
            Long longValue = rs.getLong(idField);

            id = new RdbmsId(domainObjectType, longValue);

        }
        return id;
    }
}
