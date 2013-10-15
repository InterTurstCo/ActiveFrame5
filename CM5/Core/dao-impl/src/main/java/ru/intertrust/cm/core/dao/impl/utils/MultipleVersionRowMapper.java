package ru.intertrust.cm.core.dao.impl.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;


public class MultipleVersionRowMapper extends BasicRowMapper implements ResultSetExtractor<List<DomainObjectVersion>> {

    public MultipleVersionRowMapper(String domainObjectType, String idField, ConfigurationExplorer configurationExplorer,
            DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, idField, configurationExplorer, domainObjectTypeIdCache);
    }


    @Override
    public List<DomainObjectVersion> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<DomainObjectVersion> result = new ArrayList<DomainObjectVersion>();

        while (rs.next()) {
            result.add(buildDomainObjectVersion(rs));
        }
        return result;
    }    
}
