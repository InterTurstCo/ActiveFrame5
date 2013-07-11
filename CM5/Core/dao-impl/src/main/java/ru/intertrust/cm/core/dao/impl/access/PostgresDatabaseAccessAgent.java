package ru.intertrust.cm.core.dao.impl.access;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;

public class PostgresDatabaseAccessAgent implements DatabaseAccessAgent {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean checkDomainObjectAccess(int userId, Id objectId, AccessType type) {
        RdbmsId id = (RdbmsId) objectId;
        String opCode = makeAccessTypeCode(type);
        // TODO Make real query to the database
        String query = "";
        return jdbcTemplate.queryForObject(query, Boolean.class);
    }

    @Override
    public boolean checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types) {
        // TODO Auto-generated method stub
        return false;
    }

    private static String makeAccessTypeCode(AccessType type) {
        if (DomainObjectAccessType.READ.equals(type)) {
            return "R";
        }
        if (DomainObjectAccessType.WRITE.equals(type)) {
            return "W";
        }
        if (DomainObjectAccessType.DELETE.equals(type)) {
            return "D";
        }
        if (CreateChildAccessType.class.equals(type.getClass())) {
            CreateChildAccessType ccType = (CreateChildAccessType) type;
            return new StringBuilder("C_").append(ccType.getChildType()).toString();
        }
        throw new IllegalArgumentException("Unknown access type: " + type);
    }
}
