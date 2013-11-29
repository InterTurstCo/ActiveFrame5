package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.model.FatalException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.wrap;

/**
 * Реализация {@link DomainObjectTypeIdDao}
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:33 PM
 */
public class DomainObjectTypeIdDaoImpl implements DomainObjectTypeIdDao {

    protected static final String INSERT_BY_NAME_QUERY =
            "insert into " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + "(" + wrap(NAME_COLUMN) + ") values (?)";

    protected static final String INSERT_BY_ID_AND_NAME_QUERY =
            "insert into " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + "(" + wrap(ID_COLUMN) + ", " +
                    wrap(NAME_COLUMN) + ") values (?, ?)";

    protected static final String SELECT_ID_BY_NAME_QUERY =
            "select " + wrap(ID_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where " +
                    wrap(NAME_COLUMN) + " = ?";

    protected static final String SELECT_NAME_BY_ID_QUERY =
            "select " + wrap(NAME_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where " +
                    wrap(ID_COLUMN) + " = ?";

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
    public Integer insert(DomainObjectTypeConfig config) {
        if (config.getDbId() == null) {
            jdbcTemplate.update(INSERT_BY_NAME_QUERY, config.getName());
            return jdbcTemplate.queryForObject(SELECT_ID_BY_NAME_QUERY, Integer.class,
                    config.getName());
        } else {
            List<String> names =
                    jdbcTemplate.queryForList(SELECT_NAME_BY_ID_QUERY, new Object[] {config.getDbId()}, String.class);

            if (names != null && !names.isEmpty()) {
                throw new FatalException("Cannot create domain object type for id " + config.getDbId() +
                " because it's already in use by domain object type with name " + config.getName());
            }

            int rowsInserted = jdbcTemplate.update(INSERT_BY_ID_AND_NAME_QUERY, config.getDbId(), config.getName());
            if (rowsInserted > 0) {
                return config.getDbId();
            } else {
                throw new FatalException("Failed to create domain object type id with id " + config.getDbId());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer findIdByName(String configName) {
        return jdbcTemplate.queryForObject(SELECT_ID_BY_NAME_QUERY, Integer.class, configName);
    }
}
