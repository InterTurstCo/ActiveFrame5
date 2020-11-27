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
import ru.intertrust.cm.core.business.api.workflow.ProcessInstanceInfo;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;
import ru.intertrust.cm.core.model.FatalException;

@ServerComponent(name = "process.variables.collection")
public class ProcessVariablesCollectionGenerator implements CollectionDataGenerator {
    @Autowired
    private ProcessService processService;

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        GenericIdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();

        List<FieldConfig> fieldConfigs = new ArrayList<>();
        fieldConfigs.add(new StringFieldConfig("name", true, false, 256, false));
        fieldConfigs.add(new StringFieldConfig("value", true, false, 256, false));
        result.setFieldsConfiguration(fieldConfigs);

        if (filters.size() == 1) {

            Id instanceId = ((ReferenceValue) filters.get(0).getParameterMap().get(0).get(0)).get();
            Map<String, Object> variables = processService.getProcessInstanceVariables(((DomainObjectMappingId) instanceId).getId(), offset, limit);

            int rowNumber = 0;
            if (variables != null) {
                for (String name : variables.keySet()) {
                    result.setId(rowNumber, new DomainObjectMappingId("process_variable", "_" + System.currentTimeMillis()));
                    result.set("name", rowNumber, new StringValue(name));
                    result.set("value", rowNumber, new StringValue(variables.get(name).toString()));

                    rowNumber++;
                }
            }
        }

        return result;
    }

    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        return 0;
    }
}
