package ru.intertrust.cm.core.dao.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;

/**
 * Реализация агента БД по запросам прав доступа для PostgreSQL.
 * <p>Перед использованием объекта необходимо установить корректный источник данных вызовом метода
 * {@link #setDataSource(DataSource)}. Обычно это делается через соотвествующий атрибут или тег конфигурации
 * объекта в Spring-контексте приложения (beans.xml), где создаётся и сам объект.
 * 
 * @author apirozhkov
 */
public class PostgresDatabaseAccessAgent implements DatabaseAccessAgent {

    private JdbcTemplate jdbcTemplate;

    /**
     * Устанавливает источник данных, который будет использоваться для выполнения запросов.
     * 
     * @param dataSource Источник данных
     */
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
    public Id[] checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type) {
        RdbmsId[] ids = (RdbmsId[]) objectIds;
        String opCode = makeAccessTypeCode(type);
        // TODO Make real query to the database
        String query = "";
        return jdbcTemplate.query(query, new RowMapper<Id>() {

            @Override
            public Id mapRow(ResultSet rs, int rowNum) throws SQLException {
                Id id = null;
                // TODO Process row
                return id;
            }
            
        }).toArray(new Id[0]);
    }

    @Override
    public AccessType[] checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types) {
        RdbmsId id = (RdbmsId) objectId;
        String[] opCodes = makeAccessTypeCodes(types);
        // TODO Make real query to the database
        String query = "";
        return jdbcTemplate.query(query, new RowMapper<AccessType>() {

            @Override
            public AccessType mapRow(ResultSet rs, int rowNum) throws SQLException {
                String code = null;
                // TODO Process row
                return decodeAccessType(code);
            }
        }).toArray(new AccessType[0]);
    }

    private static String makeAccessTypeCode(AccessType type) {
        // Разрешения на чтение хранятся в отдельной таблице, поэтому код "R" не используется
        /*if (DomainObjectAccessType.READ.equals(type)) {
            return "R";
        }*/
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

    private static String[] makeAccessTypeCodes(AccessType[] types) {
        String[] codes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = makeAccessTypeCode(types[i]);
        }
        return codes;
    }

    private static AccessType decodeAccessType(String code) {
        String[] codeParts = code.split("_", 2);
        if ("W".equals(codeParts[0])) {
            return DomainObjectAccessType.WRITE;
        }
        if ("D".equals(codeParts[0])) {
            return DomainObjectAccessType.DELETE;
        }
        if ("C".equals(codeParts[0])) {
            return new CreateChildAccessType(codeParts[1]);
        }
        throw new IllegalArgumentException("Unknown access type: " + code);
    }

    @Override
    public boolean checkUserGroup(int userId, String groupName) {
        // TODO make real query to the database
        return false;
    }
}
