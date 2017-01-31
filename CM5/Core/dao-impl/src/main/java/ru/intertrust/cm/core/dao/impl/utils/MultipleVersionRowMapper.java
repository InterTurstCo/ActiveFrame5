package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.impl.ResultSetExtractionLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MultipleVersionRowMapper extends BasicVersionRowMapper implements ResultSetExtractor<List<DomainObjectVersion>> {

    public MultipleVersionRowMapper(String domainObjectType, String idField, ConfigurationExplorer configurationExplorer,
            DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectType, idField, configurationExplorer, domainObjectTypeIdCache);
    }


    @Override
    public List<DomainObjectVersion> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<DomainObjectVersion> result = new ArrayList<DomainObjectVersion>();

        ColumnModel columnModel = buildColumnModel(rs);
        long rowCount = 0;
        final long start = System.currentTimeMillis();
        while (rs.next()) {
            ResultSetExtractionLogger.log("MultipleVersionRowMapper.extractData", start, ++rowCount);
            result.add(buildDomainObjectVersion(rs, columnModel));
        }
        return result;
    }    
}
