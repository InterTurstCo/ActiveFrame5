package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.ResultSetExtractionLogger;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Отображает {@link java.sql.ResultSet} на доменный объект {@link ru.intertrust.cm.core.business.api.dto.DomainObject}.
 *
 * @author atsvetkov
 */
@SuppressWarnings("rawtypes")
public class SingleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<DomainObject> {

    public SingleObjectRowMapper(String domainObjectType, ConfigurationExplorer configurationExplorer,
                                 DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, DefaultFields.DEFAULT_ID_FIELD, configurationExplorer, domainObjectTypeIdCache);
    }

    @Override
    public DomainObject extractData(ResultSet rs) throws SQLException, DataAccessException {
        DomainObject object = null;

        ColumnModel columnModel = buildColumnModel(rs);
        long rowCount = 0;
        final long start = System.currentTimeMillis();
        while (rs.next()) {
            ResultSetExtractionLogger.log("SingleObjectRowMapper.extractData", start, ++rowCount);
            object = buildDomainObject(rs, columnModel);
        }
        return object;
    }
}
