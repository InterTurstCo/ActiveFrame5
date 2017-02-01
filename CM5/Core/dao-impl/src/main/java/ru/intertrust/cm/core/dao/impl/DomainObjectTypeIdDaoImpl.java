package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.model.FatalException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация {@link DomainObjectTypeIdDao}
 * @author vmatsukevich
 *         Date: 8/13/13
 *         Time: 2:33 PM
 */
public class DomainObjectTypeIdDaoImpl implements DomainObjectTypeIdDao {

    protected static final String INSERT_QUERY =
            "insert into " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + "(" + wrap(ID_COLUMN) + ", " +
                    wrap(NAME_COLUMN) + ") values (?, ?)";

    protected static final String DELETE_QUERY =
            "delete from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where name = ?";

    protected static final String SELECT_ID_BY_NAME_QUERY =
            "select " + wrap(ID_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where " +
                    wrap(NAME_COLUMN) + " = ?";

    protected static final String SELECT_NAME_BY_ID_QUERY =
            "select " + wrap(NAME_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where " +
                    wrap(ID_COLUMN) + " = ?";

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcTemplate;

    @Autowired
    private IdGenerator idGenerator;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainObjectTypeId> readAll() {
        String query = "select " + wrap(ID_COLUMN) + ", " + wrap(NAME_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE);

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
            Object domainObjectTypeId = idGenerator.generateId(DOMAIN_OBJECT_TYPE_ID_TABLE);
            jdbcTemplate.update(INSERT_QUERY, domainObjectTypeId, config.getName());
            return ((Long) domainObjectTypeId).intValue();
        } else {
            List<String> names =
                    jdbcTemplate.queryForList(SELECT_NAME_BY_ID_QUERY, new Object[] {config.getDbId()}, String.class);

            if (names != null && !names.isEmpty()) {
                throw new FatalException("Cannot create domain object type for id " + config.getDbId() +
                " because it's already in use by domain object type with name " + config.getName());
            }

            int rowsInserted = jdbcTemplate.update(INSERT_QUERY, config.getDbId(), config.getName());
            if (rowsInserted > 0) {
                return config.getDbId();
            } else {
                throw new FatalException("Failed to create domain object type id with id " + config.getDbId());
            }
        }
    }

    @Override
    public Integer delete(DomainObjectTypeConfig config) {
        int rowsDeleted = jdbcTemplate.update(DELETE_QUERY, config.getName());
        if (rowsDeleted > 0) {
            return rowsDeleted;
        } else {
            throw new FatalException("Failed to delete domain object type with name: " + config.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer findIdByName(String configName) {
        List<Integer> ids = jdbcTemplate.queryForList(SELECT_ID_BY_NAME_QUERY, Integer.class, configName);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
         return ids.get(0);
    }
}
