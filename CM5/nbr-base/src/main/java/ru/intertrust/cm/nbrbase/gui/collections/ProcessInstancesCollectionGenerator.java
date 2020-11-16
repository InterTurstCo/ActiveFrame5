package ru.intertrust.cm.nbrbase.gui.collections;

import java.util.ArrayList;
import java.util.List;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;

@ServerComponent(name = "process.instances.collection")
public class ProcessInstancesCollectionGenerator implements CollectionDataGenerator {

    @Autowired
    private ProcessService processService;

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        GenericIdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();

        List<ProcessInstanceInfo> instancesInfos = processService.getProcessInstanceInfos(offset, limit);

        List<FieldConfig> fieldConfigs = new ArrayList<>();
        fieldConfigs.add(new StringFieldConfig("name", true, false, 256, false));
        fieldConfigs.add(new DateTimeFieldConfig("start_date", false, false));
        fieldConfigs.add(new DateTimeFieldConfig("finish_date", false, false));
        result.setFieldsConfiguration(fieldConfigs);

        int rowNumber = 0;
        for (ProcessInstanceInfo instancesInfo : instancesInfos) {
            result.setId(rowNumber, new DomainObjectMappingId("process_instance", instancesInfo.getId()));
            result.set("name", rowNumber, new StringValue(instancesInfo.getName()));
            result.set("start_date", rowNumber, new DateTimeValue(instancesInfo.getStart()));
            result.set("finish_date", rowNumber, new DateTimeValue(instancesInfo.getFinish()));

            rowNumber++;
        }

        return result;
    }

    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        return 0;
    }
}
