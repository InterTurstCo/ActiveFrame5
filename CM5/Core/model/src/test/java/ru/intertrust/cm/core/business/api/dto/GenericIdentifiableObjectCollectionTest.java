package ru.intertrust.cm.core.business.api.dto;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

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
        ArrayList<String> fields = new ArrayList<>();
        fields.add("Type1");
        fields.add("Type2");
        fields.add("Type3");
        collection.setFields(fields);

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
        Id id = new RdbmsId("unique_key", 1000);
        collection.setId(TEST_ROW, id);
        assertEquals(id, collection.get(TEST_ROW).getId());

    }
}
