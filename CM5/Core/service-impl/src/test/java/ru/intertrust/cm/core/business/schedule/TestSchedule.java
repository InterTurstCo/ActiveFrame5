package ru.intertrust.cm.core.business.schedule;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.ScheduleService;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.shedule.ScheduleTaskLoaderImpl;

@RunWith(MockitoJUnitRunner.class)
public class TestSchedule {
    @Mock
    private ClusterManager clusterManager;
    
    @InjectMocks
    private ScheduleTaskLoaderImpl taskLoaderImpl = new ScheduleTaskLoaderImpl();
    
    private class ScheduleTaskLoaderImplInt extends ScheduleTaskLoaderImpl {
        public String getDefaultParametersInt(ScheduleTask configuration) {
            return getDefaultParameters(configuration);
        }
    }

    @ScheduleTask(name = "TestScheduleWithDefaultParams", minute = "*/1", configClass = DefaultParameter.class)
    public class TestScheduleWithDefaultParams implements ScheduleTaskHandle {

        @Override
        public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) {
            return "OK";
        }

    }

    @Test
    public void testLoadDefaultParameters() throws ParseException {
        String etalon = "<scheduleTaskConfig>\n";
        etalon += "   <parameters class=\"ru.intertrust.cm.core.business.schedule.Parameters\">\n";
        etalon += "      <parameters result=\"OK\"/>\n";
        etalon += "   </parameters>\n";
        etalon += "</scheduleTaskConfig>";

        ScheduleTaskLoaderImplInt sheduleTaskLoader = new ScheduleTaskLoaderImplInt();

        ScheduleTask configuration = TestScheduleWithDefaultParams.class.getAnnotation(ScheduleTask.class);

        String result = sheduleTaskLoader.getDefaultParametersInt(configuration);
        Assert.assertTrue("Check schedulr param", result.equals(etalon));
    }
    
    @Test
    public void testClusterGetNextNode() throws ParseException {
        Set<String> nodes = new HashSet<String>();
        //Одна доступная нода
        nodes.add("aaa");
        when(clusterManager.getNodesWithRole(ScheduleService.SCHEDULE_EXECUTOR_ROLE_NAME)).thenReturn(nodes);
        
        String nodeId1, nodeId2, nodeId3;
        
        nodeId1 = taskLoaderImpl.getNextNodeId();
        Assert.assertTrue("Check return node", nodeId1 != null);
        
        //2 доступных ноды
        nodes.add("bbb");
        nodeId1 = taskLoaderImpl.getNextNodeId();
        nodeId2 = taskLoaderImpl.getNextNodeId();
        
        Assert.assertTrue("Check return node", nodeId1 != null && nodeId2 != null);
        Assert.assertTrue("Check node different", !nodeId1.equals(nodeId2));

        //3 доступных ноды
        nodes.add("ccc");
        nodeId1 = taskLoaderImpl.getNextNodeId();
        nodeId2 = taskLoaderImpl.getNextNodeId();
        nodeId3 = taskLoaderImpl.getNextNodeId();
        
        Assert.assertTrue("Check return node", nodeId1 != null && nodeId2 != null && nodeId3 != null);
        Assert.assertTrue("Check node different", !nodeId1.equals(nodeId2) && !nodeId1.equals(nodeId3) && !nodeId2.equals(nodeId3));

    }
}
