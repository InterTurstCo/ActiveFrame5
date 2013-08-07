package ru.intertrust.cm.core.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.dao.exception.CollectionConfigurationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author vmatsukevich
 *         Date: 7/2/13
 *         Time: 12:55 PM
 */
public class CollectionsDaoImplTest {

    private static final String COLLECTION_COUNT_WITH_FILTERS =
            "select count(*) from employee e inner join department d on e.department = d.id " +
                    "WHERE 1=1 and d.name = 'dep1' and e.name = 'employee1'";

    private static final String COLLECTION_QUERY_WITH_LIMITS =
            "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e " +
                    "inner join department d on e.department = d.id " +
                    "where 1=1 and d.name = 'dep1' order by e.name asc limit 100 OFFSET 10";

    private static final String FIND_COLLECTION_QUERY_WITH_FILTERS =
            "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e " +
                    "inner join department d on e.department = d.id where 1=1 and d.name = 'dep1' order by e.name asc";

    private static final String FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS =
            "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e inner join department d" +
            " on e.department = d.id inner join authentication_info a on e.login = a.id where 1=1 and d.name = 'dep1' " +
            "and e.name = 'employee1'" +
            " and a.id = 1 order by e.name asc";

    private static final String COLLECTION_QUERY_WITHOUT_FILTERS =
             "select e.id, e.name, e.position, e.created_date, e.updated_date from employee e where 1=1 order by e.name asc";
    
    private static final String EMLOYEES_PROROTYPE = "select\n" +
            "                    e.id, e.name, e.position, e.created_date, e.updated_date\n" +
            "                from\n" +
            "                    employee e\n" +
            "                     ::from-clause\n" +
            "                where\n" +
            "                    1=1 ::where-clause";

    private static final String EMPLOYEES_COUNTING_PROTOTYPE = "select count(*) from employee e ::from-clause WHERE " +
            "1=1 ::where-clause";

    private static final String EMPLOYEES_COMPLEX_PROTOTYPE = "select\n" +
            "                    e.id, e.name, e.position, e.created_date, e.updated_date\n" +
            "                from\n" +
            "                    employee e\n" +
            "                     ::from-clause1 ::from-clause2\n" +
            "                where\n" +
            "                    1=1 ::where-clause1 ::where-clause2";

    @InjectMocks
    private CollectionsDaoImpl collectionsDaoImpl = new CollectionsDaoImpl();

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ConfigurationExplorerImpl configurationExplorer;

    private CollectionFilterConfig byDepartmentFilterConfig;
    private CollectionFilterConfig byDepartmentComplexFilterConfig;
    private CollectionFilterConfig byNameFilterConfig;
    private CollectionFilterConfig byNameComplexFilterConfig;
    private CollectionFilterConfig byAuthenticationInfoFilterConfig;

    private CollectionConfig collectionConfig;
    private CollectionConfig complexCollectionConfig;

    private SortOrder sortOrder;

    @Before
    public void setUp() throws Exception {
        byDepartmentFilterConfig = createByDepartmentFilterConfig();
        byDepartmentComplexFilterConfig = createByDepartmentComplexFilterConfig();

        byNameFilterConfig = createByNameFilterConfig();
        byNameComplexFilterConfig = createByNameComplexFilterConfig();
        byAuthenticationInfoFilterConfig = createbyAuthenticationInfoFilterConfig();

        sortOrder = createByNameSortOrder();

        collectionConfig = createEmployeesCollectionConfig();
        complexCollectionConfig = createEmployeesComplexCollectionConfig();
    }

    @Test
    public void testFindCollectionWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        filledFilterConfigs.add(byDepartmentFilterConfig);

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 0, 0);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
    }

    @Test
    public void testFindCopmplexCollectionWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();

        filledFilterConfigs.add(byDepartmentComplexFilterConfig);
        filledFilterConfigs.add(byNameComplexFilterConfig);
        filledFilterConfigs.add(byAuthenticationInfoFilterConfig);

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(complexCollectionConfig, filledFilterConfigs, sortOrder, 0, 0);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(FIND_COMPLEX_COLLECTION_QUERY_WITH_FILTERS, refinedActualQuery);
    }

    @Test
    public void testFindCollectionWithoutFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 0, 0);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_QUERY_WITHOUT_FILTERS, refinedActualQuery);
    }

    @Test
    public void testFindCollectionWithLimits() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        filledFilterConfigs.add(byDepartmentFilterConfig);

        String actualQuery = collectionsDaoImpl.getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, 10, 100);
        String refinedActualQuery = refineQuery(actualQuery);

        assertEquals(COLLECTION_QUERY_WITH_LIMITS, refinedActualQuery);
    }

    @Test
    public void testFindCollectionCountWithFilters() throws Exception {
        List<CollectionFilterConfig> filledFilterConfigs = new ArrayList<>();
        filledFilterConfigs.add(byDepartmentFilterConfig);
        filledFilterConfigs.add(byNameFilterConfig);

        String actualQuery = collectionsDaoImpl.getFindCollectionCountQuery(collectionConfig, filledFilterConfigs);
        String refinedActualQuery = refineQuery(actualQuery);
        assertEquals(COLLECTION_COUNT_WITH_FILTERS, refinedActualQuery);
    }

    private String refineQuery(String actualQuery) {
        return actualQuery.trim().replaceAll("\\s+", " ");
    }

    private CollectionFilterConfig createByDepartmentFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byDepartment");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause");
        collectionFilterReference.setValue("inner join department d on e.department = d.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause");
        collectionFilterCriteriaConfig.setValue(" d.name = 'dep1'");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createByDepartmentComplexFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byDepartment");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause1");
        collectionFilterReference.setValue("inner join department d on e.department = d.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause1");
        collectionFilterCriteriaConfig.setValue(" d.name = 'dep1'");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createbyAuthenticationInfoFilterConfig() {
        CollectionFilterConfig byDepartmentFilterConfig = new CollectionFilterConfig();
        byDepartmentFilterConfig.setName("byAuthenticationInfo");
        CollectionFilterReferenceConfig collectionFilterReference = new CollectionFilterReferenceConfig();

        collectionFilterReference.setPlaceholder("from-clause2");
        collectionFilterReference.setValue("inner join authentication_info a on e.login = a.id");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause2");
        collectionFilterCriteriaConfig.setValue(" a.id = 1 ");

        byDepartmentFilterConfig.setFilterReference(collectionFilterReference);
        byDepartmentFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byDepartmentFilterConfig;
    }

    private CollectionFilterConfig createByNameFilterConfig() {
        CollectionFilterConfig byNameFilterConfig = new CollectionFilterConfig();
        byNameFilterConfig.setName("byName");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause");
        collectionFilterCriteriaConfig.setValue(" e.name = 'employee1' ");

        byNameFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byNameFilterConfig;
    }

    private CollectionFilterConfig createByNameComplexFilterConfig() {
        CollectionFilterConfig byNameFilterConfig = new CollectionFilterConfig();
        byNameFilterConfig.setName("byName");

        CollectionFilterCriteriaConfig collectionFilterCriteriaConfig = new CollectionFilterCriteriaConfig();
        collectionFilterCriteriaConfig.setCondition(" and ");
        collectionFilterCriteriaConfig.setPlaceholder("where-clause1");
        collectionFilterCriteriaConfig.setValue(" e.name = 'employee1' ");

        byNameFilterConfig.setFilterCriteria(collectionFilterCriteriaConfig);
        return byNameFilterConfig;
    }

    private SortOrder createByNameSortOrder() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.name", SortCriterion.Order.ASCENDING));
        return sortOrder;
    }

    private CollectionConfig createEmployeesCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("Employees");
        result.setPrototype(EMLOYEES_PROROTYPE);
        result.setCountingPrototype(EMPLOYEES_COUNTING_PROTOTYPE);
        result.getFilters().add(byDepartmentFilterConfig);

        return result;
    }

    private CollectionConfig createEmployeesComplexCollectionConfig() {
        CollectionConfig result = new CollectionConfig();
        result.setName("EmployeesComplex");
        result.setPrototype(EMPLOYEES_COMPLEX_PROTOTYPE);
        result.setCountingPrototype(EMPLOYEES_COUNTING_PROTOTYPE);
        result.getFilters().add(byDepartmentFilterConfig);

        return result;
    }
}
