package ru.intertrust.cm.core.gui.impl.server.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

/**
 * Тестовый генератор коллекции
 * @author atsvetkov
 *
 */
@ServerComponent(name = "plugins.collection")
public class PluginsCollectionGenerator implements CollectionDataGenerator {
    private static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String ID_FIELD = "id";

    @Autowired
    private PluginService pluginService;

    @Override
    public IdentifiableObjectCollection findCollection(List<? extends Filter> filters, SortOrder sortOrder, int offset, int limit) {
        GenericIdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();
        List<FieldConfig> fieldConfigs = new ArrayList<FieldConfig>();
        FieldConfig idField = new ReferenceFieldConfig();
        idField.setName(ID_FIELD);
        fieldConfigs.add(idField);
        
        FieldConfig nameField = new StringFieldConfig();
        nameField.setName(NAME_FIELD);
        fieldConfigs.add(nameField);
        
        FieldConfig descriptionField = new StringFieldConfig();
        descriptionField.setName(DESCRIPTION_FIELD);
        fieldConfigs.add(descriptionField);
        
        result.setFieldsConfiguration(fieldConfigs);

        int i = 0;
        Collection<PluginInfo> pluginInfos = pluginService.getPlugins().values();
        for (PluginInfo pluginInfo : pluginInfos) {
            result.set(NAME_FIELD, i, new StringValue(pluginInfo.getName()));
            result.set(DESCRIPTION_FIELD, i, new StringValue(pluginInfo.getDescription()));
            result.setId(i, new RdbmsId(-100, i));
            i++;
        }
        
        return result;
    }

    @Override
    public int findCollectionCount(List<? extends Filter> filterValues) {
        return pluginService.getPlugins().size();
    }
}