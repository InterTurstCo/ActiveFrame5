package ru.intertrust.cm.core.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.model.FatalException;


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
            "delete from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where lower(name) = ?";

    protected static final String SELECT_ID_BY_NAME_QUERY =
            "select " + wrap(ID_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where lower(" +
                    wrap(NAME_COLUMN) + ") = ?";

    protected static final String SELECT_NAME_BY_ID_QUERY =
            "select " + wrap(NAME_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " where " +
                    wrap(ID_COLUMN) + " = ?";

    /**
     * Список не занятых идентификаторов типов.
     */
    private Set<Integer> freeTypeIds = new HashSet<>();

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DataStructureDao dataStructureDao;

    /*@Autowired
    private IdGenerator idGenerator;*/

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void init(){
        synchronized (freeTypeIds) {
            // Инициализация свободных идентификаторов типов
            freeTypeIds.clear();
            // Начинаем с 1000, так как есть места где заложились что ид типа 4 символа и вычисляют его как
            // to_char(cast((apr_apprlist.id_type * 10^12) as bigint) + apr_apprlist.id
            for (int i = 1000; i < 10000; i++) {
                freeTypeIds.add(i);
            }

            List<DomainObjectTypeId> domainObjectTypeIdConfigs = readAll();
            for (DomainObjectTypeId domainObjectTypeIdConfig : domainObjectTypeIdConfigs) {
                freeTypeIds.remove(domainObjectTypeIdConfig.getId());
            }

            Collection<DomainObjectTypeConfig> typeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
            for (DomainObjectTypeConfig typeConfig : typeConfigs) {
                if (typeConfig.getDbId() != null) {
                    freeTypeIds.remove(typeConfig.getDbId());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainObjectTypeId> readAll() {

        if (dataStructureDao.isTableExist(DOMAIN_OBJECT_TYPE_ID_TABLE)) {

            String query = "select " + wrap(ID_COLUMN) + ", " + wrap(NAME_COLUMN) + " from " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE);

            return jdbcTemplate.query(query, new RowMapper<DomainObjectTypeId>() {
                @Override
                public DomainObjectTypeId mapRow(ResultSet resultSet, int i) throws SQLException {
                    String name = resultSet.getString(NAME_COLUMN);
                    Integer id = resultSet.getInt(ID_COLUMN);
                    return new DomainObjectTypeId(name, id);
                }
            });
        }else{
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insert(DomainObjectTypeConfig config) {
        if (config.getDbId() == null) {
            Integer domainObjectTypeId = generateId();
            jdbcTemplate.update(INSERT_QUERY, domainObjectTypeId, config.getName());
            return domainObjectTypeId;
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

    private int generateId() {
        synchronized (freeTypeIds) {
            Iterator<Integer> typeIdsIterator = freeTypeIds.iterator();
            if (typeIdsIterator.hasNext()) {
                int result = typeIdsIterator.next();
                freeTypeIds.remove(result);
                return result;
            }else{
                throw new FatalException("Out of domain object ids");
            }
        }
    }

    @Override
    public Integer delete(DomainObjectTypeConfig config) {
        int rowsDeleted = jdbcTemplate.update(DELETE_QUERY, config.getName().toLowerCase());
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
        List<Integer> ids = jdbcTemplate.queryForList(SELECT_ID_BY_NAME_QUERY, Integer.class, configName.toLowerCase());
        if (ids == null || ids.isEmpty()) {
            return null;
        }
         return ids.get(0);
    }
}
