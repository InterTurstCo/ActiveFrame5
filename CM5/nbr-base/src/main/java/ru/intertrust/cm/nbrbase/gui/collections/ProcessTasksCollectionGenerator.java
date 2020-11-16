package ru.intertrust.cm.nbrbase.gui.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.workflow.TaskInfo;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;
import ru.intertrust.cm.core.model.FatalException;

@ServerComponent(name = "process.tasks.collection")
public class ProcessTasksCollectionGenerator implements CollectionDataGenerator {
    @Autowired
    private ProcessService processService;

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        GenericIdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();


        if (filters.size() != 1){
            throw new FatalException("ProcessTasksCollectionGenerator need onlyb one filter value with process instance id");
        }
        Id instanceId = ((ReferenceValue)filters.get(0).getParameterMap().get(0).get(0)).get();
        List<TaskInfo> tasks = processService.getProcessInstanceTasks(((DomainObjectMappingId)instanceId).getId(), offset, limit);

        List<FieldConfig> fieldConfigs = new ArrayList<>();
        fieldConfigs.add(new StringFieldConfig("name", true, false, 256, false));
        fieldConfigs.add(new DateTimeFieldConfig("start_date", true, false));
        fieldConfigs.add(new DateTimeFieldConfig("finish_date", true, false));
        fieldConfigs.add(new StringFieldConfig("assignee", true, false, 256, false));
        result.setFieldsConfiguration(fieldConfigs);

        int rowNumber = 0;
        if (tasks != null) {
            for (TaskInfo task : tasks) {
                result.setId(rowNumber, new DomainObjectMappingId("process_task", task.getId()));
                result.set("name", rowNumber, new StringValue(task.getName()));
                result.set("start_date", rowNumber, new DateTimeValue(task.getStartDate()));
                result.set("finish_date", rowNumber, new DateTimeValue(task.getFinishDate()));
                result.set("assignee", rowNumber, new StringValue(task.getAssignee()));

                rowNumber++;
            }
        }

        return result;
    }

    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        return 0;
    }
}
