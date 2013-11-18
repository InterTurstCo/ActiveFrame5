package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Реализация {@link DomainObjectTypeIdDao}
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:33 PM
 */
public class DomainObjectTypeIdDaoImpl implements DomainObjectTypeIdDao {

    protected static final String INSERT_INTO_DOMAIN_OBJECT_TYPE_ID_TABLE_QUERY =
            "insert into " + DOMAIN_OBJECT_TYPE_ID_TABLE + "(" + NAME_COLUMN + ") values (?)";

    protected static final String SELECT_DOMAIN_OBJECT_TYPE_ID_BY_NAME_QUERY =
            "select " + ID_COLUMN + " from " + DOMAIN_OBJECT_TYPE_ID_TABLE + " where " + NAME_COLUMN + " = ?";

    @Autowired
    private JdbcOperations jdbcTemplate;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainObjectTypeId> readAll() {
        String query = "select " + ID_COLUMN + ", " + NAME_COLUMN + " from " + DOMAIN_OBJECT_TYPE_ID_TABLE;

        return jdbcTemplate.query(query, new RowMapper<DomainObjectTypeId>() {
            @Override
            public DomainObjectTypeId mapRow(ResultSet resultSet, int i) throws SQLException {
                String name = resultSet.getString(NAME_COLUMN);
                Integer id = resultSet.getInt(ID_COLUMN);
                return new DomainObjectTypeId(name, id);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insert(String domainObjectTypeName) {
        jdbcTemplate.update(INSERT_INTO_DOMAIN_OBJECT_TYPE_ID_TABLE_QUERY, domainObjectTypeName);
        return jdbcTemplate.queryForObject(SELECT_DOMAIN_OBJECT_TYPE_ID_BY_NAME_QUERY, Integer.class,
                domainObjectTypeName);
    }
}
