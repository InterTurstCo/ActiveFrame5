package ru.intertrust.cm.core.business.api.dto;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;

/**
 * Тестовый клас для {@link GenericIdentifiableObjectCollection}
 * @author atsvetkov
 *
 */
public class GenericIdentifiableObjectCollectionTest {


    private static final int TEST_ROW = 5;
    private static final int TEST_INDEX = 2;

    private IdentifiableObjectCollection collection;

    @Before
    public void setUp() {
        collection = createStringColumnCollection("Type1", "Type2", "Type3");
    }

    @Test
    public void testFieldsOrder() {
        assertEquals("Type2", collection.getFieldsConfiguration().get(collection.getFieldIndex("Type2")).getName());
        //проверить, что естественный порядок полей сохраняется
        assertEquals(1, collection.getFieldIndex("Type2"));
    }

    @Test
    public void testGetField(){
        int fieldIndex = TEST_INDEX;
        int rowNum = TEST_ROW;
        LongValue longValue = new LongValue(5);
        collection.set(fieldIndex, rowNum, longValue);

        assertEquals(longValue, collection.get(fieldIndex, rowNum));

    }

    @Test
    public void testGetId(){
        Id id = new RdbmsId(10, 1000);
        collection.setId(TEST_ROW, id);
        assertEquals(id, collection.get(TEST_ROW).getId());
    }

    @Test
    public void testSort() {
        /*
        This table should be a result of the sort (nulls are the last)
        Sort order: type1 asc, type2 asc, type3 desc
        ----------------------------------------------
        |   Type1    |      Type2       |    Type3   |
        ----------------------------------------------
        |     A      |        U         |     Y      |
        |     A      |        U         |     X      |
        |     A      |        U         |     W      |
        |     A      |        U         |            |
        |     B      |        C         |     D      |
        |     B      |        D         |     A      |
        ----------------------------------------------

         */
        //Put rows in mixed order:
        collection.set(0, 0, new StringValue("A"));
        collection.set(1, 0, new StringValue("U"));
        collection.set(2, 0, new StringValue("X"));

        collection.set(0, 1, new StringValue("B"));
        collection.set(1, 1, new StringValue("D"));
        collection.set(2, 1, new StringValue("A"));

        collection.set(0, 2, new StringValue("B"));
        collection.set(1, 2, new StringValue("C"));
        collection.set(2, 2, new StringValue("D"));

        collection.set(0, 3, new StringValue("A"));
        collection.set(1, 3, new StringValue("U"));
        collection.set(2, 3, new StringValue("Y"));

        collection.set(0, 4, new StringValue("A"));
        collection.set(1, 4, new StringValue("U"));
        collection.set(2, 4, new StringValue("W"));

        collection.set(0, 5, new StringValue("A"));
        collection.set(1, 5, new StringValue("U"));
        collection.set(2, 5, new StringValue(null));

        SortOrder order = new SortOrder();
        order.add(new SortCriterion("Type1", SortCriterion.Order.ASCENDING));
        order.add(new SortCriterion("Type2", SortCriterion.Order.ASCENDING));
        order.add(new SortCriterion("Type3", SortCriterion.Order.DESCENDING));
        collection.sort(order);

        assertEquals(collection.get(0, 0), new StringValue("A"));
        assertEquals(collection.get(1, 0), new StringValue("U"));
        assertEquals(collection.get(2, 0), new StringValue("Y"));

        assertEquals(collection.get(0, 1), new StringValue("A"));
        assertEquals(collection.get(1, 1), new StringValue("U"));
        assertEquals(collection.get(2, 1), new StringValue("X"));

        assertEquals(collection.get(0, 2), new StringValue("A"));
        assertEquals(collection.get(1, 2), new StringValue("U"));
        assertEquals(collection.get(2, 2), new StringValue("W"));

        assertEquals(collection.get(0, 3), new StringValue("A"));
        assertEquals(collection.get(1, 3), new StringValue("U"));
        assertEquals(collection.get(2, 3), new StringValue(null));

        assertEquals(collection.get(0, 4), new StringValue("B"));
        assertEquals(collection.get(1, 4), new StringValue("C"));
        assertEquals(collection.get(2, 4), new StringValue("D"));

        assertEquals(collection.get(0, 5), new StringValue("B"));
        assertEquals(collection.get(1, 5), new StringValue("D"));
        assertEquals(collection.get(2, 5), new StringValue("A"));
    }

    @Test
    public void testAppend() {
        /*
        This table should be a result of the appending 2 collections
        ---------------------------------
        |   Type1    |      Type2       |
        ---------------------------------
        |     A      |        U         |
        |     A      |        Z         |
        |     A      |        Y         |
        --------------------------------|
                           +
        ----------------------------------------------
        |     A      |        U         |            |
        |     B      |        C         |     D      |
        |     B      |        D         |     A      |
        ----------------------------------------------
                          =
        ----------------------------------------------
        |   Type1    |      Type2       |    Type3   |
        ----------------------------------------------
        |     A      |        U         |            |
        |     A      |        Z         |            |
        |     A      |        Y         |            |
        |     A      |        U         |            |
        |     B      |        C         |     D      |
        |     B      |        D         |     A      |
        ----------------------------------------------

         */
        final GenericIdentifiableObjectCollection collection1 = createStringColumnCollection("Type1", "Type2");
        collection1.set(0, 0, new StringValue("A"));
        collection1.set(1, 0, new StringValue("U"));

        collection1.set(0, 1, new StringValue("A"));
        collection1.set(1, 1, new StringValue("Z"));

        collection1.set(0, 2, new StringValue("A"));
        collection1.set(1, 2, new StringValue("Y"));

        final GenericIdentifiableObjectCollection collection2 = createStringColumnCollection("Type1", "Type2", "Type3");
        collection2.set(0, 0, new StringValue("A"));
        collection2.set(1, 0, new StringValue("U"));
        collection2.set(2, 0, new StringValue(null));

        collection2.set(0, 1, new StringValue("B"));
        collection2.set(1, 1, new StringValue("C"));
        collection2.set(2, 1, new StringValue("D"));

        collection2.set(0, 2, new StringValue("B"));
        collection2.set(1, 2, new StringValue("D"));
        collection2.set(2, 2, new StringValue("A"));

        collection1.append(collection2);

        assertEquals(collection1.get(0, 0), new StringValue("A"));
        assertEquals(collection1.get(1, 0), new StringValue("U"));
        assertEquals(collection1.get(2, 0), null);

        assertEquals(collection1.get(0, 1), new StringValue("A"));
        assertEquals(collection1.get(1, 1), new StringValue("Z"));
        assertEquals(collection1.get(2, 1), null);

        assertEquals(collection1.get(0, 2), new StringValue("A"));
        assertEquals(collection1.get(1, 2), new StringValue("Y"));
        assertEquals(collection1.get(2, 2), null);

        assertEquals(collection1.get(0, 3), new StringValue("A"));
        assertEquals(collection1.get(1, 3), new StringValue("U"));
        assertEquals(collection1.get(2, 3), new StringValue(null));

        assertEquals(collection1.get(0, 4), new StringValue("B"));
        assertEquals(collection1.get(1, 4), new StringValue("C"));
        assertEquals(collection1.get(2, 4), new StringValue("D"));

        assertEquals(collection1.get(0, 5), new StringValue("B"));
        assertEquals(collection1.get(1, 5), new StringValue("D"));
        assertEquals(collection1.get(2, 5), new StringValue("A"));
    }

    private static GenericIdentifiableObjectCollection createStringColumnCollection(String... columnNames) {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        ArrayList<FieldConfig> fieldConfigs = new ArrayList<>();
        for (String columnName : columnNames) {
            FieldConfig fieldConfig = new StringFieldConfig();
            fieldConfig.setName(columnName);
            fieldConfigs.add(fieldConfig);
        }

        collection.setFieldsConfiguration(fieldConfigs);
        return collection;
    }
}
