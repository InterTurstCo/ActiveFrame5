package ru.intertrust.cm.core.business.impl.search.simple;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldType;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSimpleSearchUtilsTest {

    private SimpleDataConfig config = new SimpleDataConfig();

    @Before
    public void setUp() {
        List<SimpleDataFieldConfig> fields = new ArrayList<>();
        fields.add(new SimpleDataFieldConfig("string_field", SimpleDataFieldType.String, true, true, true));
        fields.add(new SimpleDataFieldConfig("string_not_multivalued_field", SimpleDataFieldType.String, true, true, false));
        fields.add(new SimpleDataFieldConfig("long_field", SimpleDataFieldType.Long, true, true, true));
        fields.add(new SimpleDataFieldConfig("boolean_field", SimpleDataFieldType.Bollean, true, true, true));
        fields.add(new SimpleDataFieldConfig("date_field", SimpleDataFieldType.Date, true, true, true));
        fields.add(new SimpleDataFieldConfig("date-time_field", SimpleDataFieldType.DateTime, true, true, true));

        config.setName("utils_test");
        config.setFields(fields);
    }

    @Test
    public void getSolrFieldName_string() {
        DefaultSimpleSearchUtils defaultSimpleSearchUtils = new DefaultSimpleSearchUtils();
        String name = defaultSimpleSearchUtils.getSolrFieldName(config, "string_field");
        assertEquals("cm_rs_string_field", name);
    }

    @Test
    public void getSolrFieldName_string_not_mv() {
        DefaultSimpleSearchUtils defaultSimpleSearchUtils = new DefaultSimpleSearchUtils();
        String name = defaultSimpleSearchUtils.getSolrFieldName(config, "string_not_multivalued_field");
        assertEquals("cm_r_string_not_multivalued_field", name);
    }

    @Test
    public void getSolrFieldName_long() {
        DefaultSimpleSearchUtils defaultSimpleSearchUtils = new DefaultSimpleSearchUtils();
        String name = defaultSimpleSearchUtils.getSolrFieldName(config, "long_field");
        assertEquals("cm_ls_long_field", name);
    }

    @Test
    public void getSolrFieldName_boolean() {
        DefaultSimpleSearchUtils defaultSimpleSearchUtils = new DefaultSimpleSearchUtils();
        String name = defaultSimpleSearchUtils.getSolrFieldName(config, "boolean_field");
        assertEquals("cm_bs_boolean_field", name);
    }

    @Test
    public void getSolrFieldName_date() {
        DefaultSimpleSearchUtils defaultSimpleSearchUtils = new DefaultSimpleSearchUtils();
        String name = defaultSimpleSearchUtils.getSolrFieldName(config, "date_field");
        assertEquals("cm_dts_date_field", name);
    }

    @Test
    public void getSolrFieldName_dateTime() {
        DefaultSimpleSearchUtils defaultSimpleSearchUtils = new DefaultSimpleSearchUtils();
        String name = defaultSimpleSearchUtils.getSolrFieldName(config, "date-time_field");
        assertEquals("cm_dts_date-time_field", name);
    }

}