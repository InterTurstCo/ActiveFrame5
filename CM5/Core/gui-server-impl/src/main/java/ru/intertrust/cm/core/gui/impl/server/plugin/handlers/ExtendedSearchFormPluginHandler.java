package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

import java.util.HashSet;

/**
 * User: IPetrov
 * Date: 09.01.14
 * Time: 10:43
 * Обработчик формы расширенного поиска
 */
@ComponentName("extended.search.form.plugin")
public class ExtendedSearchFormPluginHandler extends PluginHandler {

    @Autowired
    SearchService searchService;
    @Autowired
    private GuiService guiService;
    @Autowired
    private ApplicationContext applicationContext;

    public FormPluginData initialize(Dto initialData) {
        ExtendedSearchData extendedSearchData= (ExtendedSearchData) initialData;
        String targetDomainObject = extendedSearchData.getSearchQuery().getTargetObjectType();

        //FormPluginConfig extendedSearchFormPluginConfig = (FormPluginConfig) initialData;
        //String targetDomainObject = extendedSearchFormPluginConfig.getDomainObjectTypeToCreate();
        //final FormPluginConfig formPluginConfig = new FormPluginConfig(targetDomainObject);

        ExtendedSearchPluginHandler extendedSearchPluginHandler = (ExtendedSearchPluginHandler)
                                                                    applicationContext.getBean("extended.search.plugin");
        extendedSearchPluginHandler.initialize(initialData);

        HashSet<String> searchFormFields = extendedSearchPluginHandler.selectSearchFormFields(targetDomainObject);
        FormDisplayData form = guiService.getSearchForm(targetDomainObject, searchFormFields);
        FormPluginData formPluginData = new FormPluginData();
        formPluginData.setFormDisplayData(form);

        return formPluginData;
    }
}
