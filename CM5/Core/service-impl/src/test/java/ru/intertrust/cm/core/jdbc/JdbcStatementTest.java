package ru.intertrust.cm.core.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JdbcStatementTest {

    @Mock
    private CollectionsService collectionsService;

    @Mock
    private SochiClient client;

    @InjectMocks
    private JdbcStatement jdbcStatement;

    @Before
    public void setup() throws Exception {
        when(client.getCollectionService()).thenReturn(collectionsService);
    }

    @Test
    public void execute() throws SQLException {
        String sql = "select 1;";
        final IdentifiableObjectCollection identifiableObjects = new GenericIdentifiableObjectCollection();
        final StringFieldConfig stringFieldConfig = new StringFieldConfig();
        stringFieldConfig.setName("StringField");
        stringFieldConfig.setLength(255);
        identifiableObjects.setFieldsConfiguration(Collections.<FieldConfig>singletonList(stringFieldConfig));
        identifiableObjects.set(0, 0, new StringValue());
        when(collectionsService.findCollectionByQuery(eq(sql), any(List.class), eq(0), eq(5001))).thenReturn(identifiableObjects);

        final boolean execute = jdbcStatement.execute(sql);
        assertTrue(execute);

        final JdbcResultSet resultSet = (JdbcResultSet) jdbcStatement.getResultSet();

        // Проще проверить коллекцию
        final IdentifiableObjectCollection collection =
                (IdentifiableObjectCollection) ReflectionTestUtils.getField(resultSet, "collection");
        assertEquals(1, collection.size());

        final boolean hasNext = (boolean) ReflectionTestUtils.getField(jdbcStatement, "hasNext");
        assertFalse(hasNext);
        assertTrue(resultSet.next());
        assertFalse(resultSet.next());

        verify(collectionsService, times(1)).findCollectionByQuery(sql, Collections.<Value>emptyList(), 0, 5001);
        verify(collectionsService, times(0)).findCollectionByQuery(sql, Collections.<Value>emptyList(), 5000, 5001);
    }

    @Test
    public void execute_with_offset() throws SQLException {
        String sql = "select 1;";
        final IdentifiableObjectCollection beforeOffset = new GenericIdentifiableObjectCollection();
        final StringFieldConfig stringField = new StringFieldConfig();
        stringField.setName("StringField");
        stringField.setLength(255);
        beforeOffset.setFieldsConfiguration(Collections.<FieldConfig>singletonList(stringField));
        for (int i = 0; i < 5001; i++) {
            beforeOffset.set(0, i, new StringValue());
        }

        final IdentifiableObjectCollection withOffset = new GenericIdentifiableObjectCollection();
        withOffset.setFieldsConfiguration(Collections.<FieldConfig>singletonList(stringField));
        withOffset.set(0, 0, new StringValue());

        when(collectionsService.findCollectionByQuery(eq(sql), any(List.class), eq(0), eq(5001))).thenReturn(beforeOffset);
        when(collectionsService.findCollectionByQuery(eq(sql), any(List.class), eq(5000), eq(5001))).thenReturn(withOffset);

        final boolean execute = jdbcStatement.execute(sql);
        assertTrue(execute);

        final JdbcResultSet resultSet = (JdbcResultSet) jdbcStatement.getResultSet();

        // Проще проверить коллекцию
        IdentifiableObjectCollection collection =
                (IdentifiableObjectCollection) ReflectionTestUtils.getField(resultSet, "collection");
        assertEquals(5000, collection.size());

        assertTrue((boolean) ReflectionTestUtils.getField(jdbcStatement, "hasNext"));
        for (int i = 0; i < 5000; ++i) {
            assertTrue(resultSet.next());
        }

        assertTrue(resultSet.next());
        assertFalse((boolean) ReflectionTestUtils.getField(jdbcStatement, "hasNext"));
        collection = (IdentifiableObjectCollection) ReflectionTestUtils.getField(resultSet, "collection");
        assertEquals(1, collection.size());

        assertFalse(resultSet.next());

        verify(collectionsService, times(1)).findCollectionByQuery(sql, Collections.<Value>emptyList(), 0, 5001);
        verify(collectionsService, times(1)).findCollectionByQuery(sql, Collections.<Value>emptyList(), 5000, 5001);
        verify(collectionsService, times(0)).findCollectionByQuery(sql, Collections.<Value>emptyList(), 10000, 5001);
    }
}