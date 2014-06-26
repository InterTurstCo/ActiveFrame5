package ru.intertrust.cm.core.dao.impl.doel;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.SystemSubject;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.StatusDao;

@RunWith(MockitoJUnitRunner.class)
public class StatusFunctionTest {

    @Mock CollectionsDao collectionsDao;
    @Mock ConfigurationExplorer configurationExplorer;
    @Mock DomainObjectTypeIdCache typeIdCache;
    @Mock DomainObjectDao domainObjectDao;
    @Mock StatusDao statusDao;
    @Mock AccessControlService accessControlService;

    @InjectMocks
    StatusFunction testee = new StatusFunction();

    @Test
    @SuppressWarnings({ "rawtypes" })
    public void testMainImplementation() {
        List<Value> context = new ArrayList<>();
        context.add(new ReferenceValue(new RdbmsId(11, 1001)));
        context.add(new ReferenceValue(new RdbmsId(11, 1002)));
        context.add(new ReferenceValue(new RdbmsId(12, 1003)));
        context.add(new ReferenceValue(new RdbmsId(12, 1004)));
        context.add(new ReferenceValue(new RdbmsId(13, 1005)));
        context.add(new ReferenceValue(new RdbmsId(21, 1006)));
        context.add(new ReferenceValue(new RdbmsId(21, 1007)));
        String params[] = new String[] { "ok1", "ok2" };
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.getSubject()).thenReturn(new SystemSubject("test"));

        when(typeIdCache.getName(anyInt())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Integer typeId = (Integer) invocation.getArguments()[0];
                return "type" + typeId;
            }
        });
        when(configurationExplorer.getDomainObjectRootType("type11")).thenReturn("type11");
        when(configurationExplorer.getDomainObjectRootType("type12")).thenReturn("type11");
        when(configurationExplorer.getDomainObjectRootType("type13")).thenReturn("type11");
        when(configurationExplorer.getDomainObjectRootType("type21")).thenReturn("type21");

        IdentifiableObjectCollection coll11 = mock(IdentifiableObjectCollection.class);
        when(coll11.get(0, 0)).thenReturn(new ReferenceValue(new RdbmsId(11, 1001)));
        when(coll11.get(0, 1)).thenReturn(new ReferenceValue(new RdbmsId(12, 1004)));
        when(coll11.size()).thenReturn(2);
        when(collectionsDao.findCollectionByQuery(contains("type11"), anyListOf(Value.class), anyInt(), anyInt(),
                any(AccessToken.class))).thenReturn(coll11);
        IdentifiableObjectCollection coll21 = mock(IdentifiableObjectCollection.class);
        when(coll21.get(0, 0)).thenReturn(new ReferenceValue(new RdbmsId(21, 1006)));
        when(coll21.size()).thenReturn(1);
        when(collectionsDao.findCollectionByQuery(contains("type21"), anyListOf(Value.class), anyInt(), anyInt(),
                any(AccessToken.class))).thenReturn(coll21);

        List<Value> result = (List<Value>) testee.process(context, params, accessToken);

        assertEquals(3, result.size());
        assertThat(result, containsInAnyOrder((Value)
                new ReferenceValue(new RdbmsId(11, 1001)),
                new ReferenceValue(new RdbmsId(12, 1004)),
                new ReferenceValue(new RdbmsId(21, 1006))));

        String query = "SELECT t.id FROM \"type11\" t JOIN status s ON t.status=s.id "
                + "WHERE (s.name='ok1' OR s.name='ok2') AND (t.id=? OR t.id=? OR t.id=? OR t.id=? OR t.id=?)";
        Matcher<Iterable<? extends Value>> idMatcher = containsInAnyOrder((Value)
                new ReferenceValue(new RdbmsId(11, 1001)),
                new ReferenceValue(new RdbmsId(11, 1002)),
                new ReferenceValue(new RdbmsId(12, 1003)),
                new ReferenceValue(new RdbmsId(12, 1004)),
                new ReferenceValue(new RdbmsId(13, 1005)));
        verify(collectionsDao).findCollectionByQuery(eq(query), (List<? extends Value>) argThat(idMatcher),
                eq(0), eq(5), same(accessToken));
        query = "SELECT t.id FROM \"type21\" t JOIN status s ON t.status=s.id "
                + "WHERE (s.name='ok1' OR s.name='ok2') AND (t.id=? OR t.id=?)";
        idMatcher = containsInAnyOrder((Value)
                new ReferenceValue(new RdbmsId(21, 1006)),
                new ReferenceValue(new RdbmsId(21, 1007)));
        verify(collectionsDao).findCollectionByQuery(eq(query), (List<? extends Value>) argThat(idMatcher),
                eq(0), eq(2), same(accessToken));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testUserTokenReplacement() {
        List<Value> context = new ArrayList<>();
        context.add(new ReferenceValue(new RdbmsId(11, 1001)));
        context.add(new ReferenceValue(new RdbmsId(22, 1002)));
        String params[] = new String[] { "status" };
        AccessToken accessToken = mock(AccessToken.class);
        UserSubject subject = mock(UserSubject.class);
        when(accessToken.getSubject()).thenReturn(subject);
        when(subject.getName()).thenReturn("user");

        when(typeIdCache.getName(anyInt())).thenReturn("type");
        when(configurationExplorer.getDomainObjectRootType("type")).thenReturn("root_type");
        AccessToken collectionAccessToken = mock(AccessToken.class);
        when(accessControlService.createCollectionAccessToken(eq("user"))).thenReturn(collectionAccessToken);
        when(collectionsDao.findCollectionByQuery(anyString(), anyListOf(Value.class), anyInt(), anyInt(),
                any(AccessToken.class))).then(RETURNS_MOCKS);

        /*List<Value> result = (List<Value>)*/ testee.process(context, params, accessToken);

        verify(collectionsDao).findCollectionByQuery(anyString(), anyListOf(Value.class), anyInt(), anyInt(),
                same(collectionAccessToken));
    }

    //@Test
    public void testSimpleImplementation() {
        //TODO not implemented
    }
}
