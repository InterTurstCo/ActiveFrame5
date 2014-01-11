package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;

import java.util.*;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 15:58
 * Обработчик команд расширенного поиска
 */
@ComponentName("extended.search.plugin")
public class ExtendedSearchPluginHandler extends PluginHandler {

    @Autowired
    ConfigurationService configurationService;

    @Override
    public  ExtendedSearchPluginData initialize(Dto params) {
        // область поиска - список целевых ДО
        HashMap<String, ArrayList<String>> searchAreas = new HashMap<String, ArrayList<String>>();
        // целевой ДО - список его полей
        HashMap<String, ArrayList<String>> searchFields = new HashMap<String, ArrayList<String>>();

        ExtendedSearchPluginData extendedSearchPluginData = new ExtendedSearchPluginData();
        Collection<SearchAreaConfig> searchAreaConfigs = configurationService.getConfigs(SearchAreaConfig.class);

        for(SearchAreaConfig searchAreaConfig  : searchAreaConfigs){
            List<TargetDomainObjectConfig> targetObjects = searchAreaConfig.getTargetObjects();
            Iterator it = targetObjects.iterator();
            // список целевых ДО в конкретной области поиска
            ArrayList<String> arrayTargetObjects = new ArrayList<String>();

            while(it.hasNext()) {
                TargetDomainObjectConfig t = (TargetDomainObjectConfig) it.next();
                List<IndexedFieldConfig> fields = t.getFields();
                ArrayList<String> fieldNames = new ArrayList<String>(fields.size());
                for (Iterator<IndexedFieldConfig> j = fields.iterator(); j.hasNext();)
                    fieldNames.add(j.next().getName());
                searchFields.put(t.getType(), fieldNames);
                arrayTargetObjects.add(t.getType());
            }

            searchAreas.put(searchAreaConfig.getName(), arrayTargetObjects);
        }

        extendedSearchPluginData.setSearchAreasData(searchAreas);
        extendedSearchPluginData.setSearchFieldsData(searchFields);

        return extendedSearchPluginData;
    }

    // "отдача" конфигураций расширенного поиска
    public ExtendedSearchPluginData searchConfigurations(Dto dto) {
        return initialize(dto);
    }

}
