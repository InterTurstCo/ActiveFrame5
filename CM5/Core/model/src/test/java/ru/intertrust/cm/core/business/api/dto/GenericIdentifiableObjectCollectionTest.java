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
        collection = new GenericIdentifiableObjectCollection();
        ArrayList<FieldConfig> fieldConfigs = new ArrayList<>();
        FieldConfig fieldConfig = new StringFieldConfig();
        fieldConfig.setName("Type1");
        
        fieldConfigs.add(fieldConfig);
        
        fieldConfig = new StringFieldConfig();
        fieldConfig.setName("Type2");
        
        fieldConfigs.add(fieldConfig);
        fieldConfig = new StringFieldConfig();
        fieldConfig.setName("Type3");
        
        fieldConfigs.add(fieldConfig);
        collection.setFieldsConfiguration(fieldConfigs);        
    }

    @Test
    public void testFieldsOrder() {
        assertEquals("Type2", collection.getFields().get(collection.getFieldIndex("Type2")));
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
}
