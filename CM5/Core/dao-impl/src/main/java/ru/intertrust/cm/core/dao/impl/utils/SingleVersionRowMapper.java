package ru.intertrust.cm.core.dao.impl.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;


public class SingleVersionRowMapper extends BasicRowMapper implements ResultSetExtractor<DomainObjectVersion> {

    public SingleVersionRowMapper(String domainObjectType, String idField, ConfigurationExplorer configurationExplorer,
            DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, idField, configurationExplorer, domainObjectTypeIdCache);
    }


    @Override
    public DomainObjectVersion extractData(ResultSet rs) throws SQLException, DataAccessException {
        DomainObjectVersion result = null;

        if (rs.next()) {
            result = buildDomainObjectVersion(rs);
        }
        return result;
    }    
}
