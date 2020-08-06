package ru.intertrust.cm.core.business.impl;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.SimpeDataStorage;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.simpledata.EqualSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleData;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldType;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SimpeDataStorageTest {

    @Mock
    private SolrServer solrServer;
    @Mock
    private ConfigurationExplorer configurationExplorer;

    @InjectMocks
    private SimpeDataStorage storage = new SimpeDataStorageImpl();

    @Before
    public void init() throws Exception{
        SimpleDataConfig config = new SimpleDataConfig();
        config.setName("test-simple-data");
        List<SimpleDataFieldConfig> fields = new ArrayList<>();
        fields.add(new SimpleDataFieldConfig("test-string", SimpleDataFieldType.String, true, true, true));
        fields.add(new SimpleDataFieldConfig("test-long", SimpleDataFieldType.Long, true, true, true));
        fields.add(new SimpleDataFieldConfig("test-boolean", SimpleDataFieldType.Bollean, true, true, true));
        fields.add(new SimpleDataFieldConfig("test-date", SimpleDataFieldType.Date, true, true, true));
        fields.add(new SimpleDataFieldConfig("test-date-time", SimpleDataFieldType.DateTime, true, true, true));
        config.setFields(fields);
        when(configurationExplorer.getConfig(eq(SimpleDataConfig.class), eq("test-simple-data"))).
                thenReturn(config);

        QueryResponse response = new QueryResponse();
        ReflectionTestUtils.setField(response, "_results", new SolrDocumentList());
        when(solrServer.query(anyObject())).thenReturn(response);
    }

    @Test
    public void testSave() throws Exception{
        SimpleData data = new SimpleData("test-simple-data");
        data.setString("test-string", "xxx", "yyy", "zzz");
        data.setBoolean("test-boolean", true, false);
        data.setLong("test-long", 10, 20);
        Date date1 = new Date();
        Date date2 = new Date();
        data.setDateTime("test-date-time", date1, date2);
        TimelessDate timelessDate1= new TimelessDate(2020, 5,9);
        TimelessDate timelessDate2= new TimelessDate(2020, 7,10);
        data.setTimelessDate("test-date", timelessDate1, timelessDate2);
        storage.save(data);

        verify(solrServer).request(argThat(new BaseMatcher<UpdateRequest>(){

            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                assertTrue(item != null && item instanceof UpdateRequest);
                UpdateRequest saveRequest = (UpdateRequest)item;
                assertTrue(saveRequest.getDocuments().size() == 1);
                SolrInputDocument doc = saveRequest.getDocuments().get(0);

                boolean result = true;
                result = result && doc.getFieldValue("id").equals(data.getId());
                result = result && doc.getFieldValue("cm_type").equals("test-simple-data");
                result = result && doc.getFieldValues("cm_rs_test-string").containsAll(Arrays.asList("xxx", "yyy", "zzz"));
                result = result && doc.getFieldValues("cm_bs_test-boolean").containsAll(Arrays.asList(true, false));
                result = result && doc.getFieldValues("cm_ls_test-long").containsAll(Arrays.asList(10L, 20L));
                result = result && doc.getFieldValues("cm_dts_test-date-time").containsAll(Arrays.asList(date1, date2));
                result = result && doc.getFieldValues("cm_dts_test-date").containsAll(Arrays.asList(timelessDate1, timelessDate2));

                return result;
            }
        }));

        verify(solrServer).commit();
    }

    @Test
    public void testFindByType() throws Exception{
        storage.find("test-simple-data", null, null, null);
        verify(solrServer).query(argThat(new SolrQueryMatcher("cm_type: \"test-simple-data\"")));
    }

    @Test
    public void testFindByFilter() throws Exception{
        storage.find("test-simple-data", Arrays.asList(new EqualSimpleDataSearchFilter("test-string", new StringValue("xxx"))), null, null);
        verify(solrServer).query(argThat(new SolrQueryMatcher("cm_type: \"test-simple-data\" AND cm_rs_test-string: \"xxx\"")));
    }

    @Test
    public void testDeleteByFilter() throws Exception{
        storage.delete("test-simple-data", Arrays.asList(new EqualSimpleDataSearchFilter("test-string", new StringValue("xxx"))));
        verify(solrServer).deleteByQuery("cm_type: \"test-simple-data\" AND cm_rs_test-string: \"xxx\"");
    }

    @Test
    public void testDeleteById() throws Exception{
        storage.delete("0123456789");
        verify(solrServer).deleteById("0123456789");
    }

    @Test
    public void findById() throws Exception{
        storage.find("test-simple-data", "0123456789");
        verify(solrServer).query(argThat(new SolrQueryMatcher("id: \"0123456789\"")));
    }

    public class SolrQueryMatcher extends BaseMatcher<SolrQuery> {
        private String query;
        public SolrQueryMatcher(String query){
            this.query = query;
        }

        @Override
        public boolean matches(Object item) {
            assertTrue(item != null && item instanceof SolrQuery);
            SolrQuery solrQuery = (SolrQuery)item;
            boolean result = solrQuery.getQuery().equals(query);
            return result;
        }

        @Override
        public void describeTo(Description description) {
        }
    }

}
