package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.plugin.*;

import java.util.*;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 15:58
 * Обработчик плагина расширенного поиска
 * получает данные о конфигурации поиска и вызывает сервис поиска
 */
@ComponentName("extended.search.plugin")
public class ExtendedSearchPluginHandler extends PluginHandler {

    @Autowired
    ConfigurationService configurationService;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    SearchService searchService;
    @Autowired
    private CrudService crudService;
    @Autowired
    private FormResolver formResolver;

    private String userUid;

    protected ExtendedSearchPluginData extendedSearchPluginData;

    @Override
    public  ExtendedSearchPluginData initialize(Dto params) {
        // область поиска - список целевых ДО
        HashMap<String, ArrayList<String>> searchAreas = new HashMap<String, ArrayList<String>>();
        // целевой ДО - список его полей
        HashMap<String, ArrayList<String>> searchFields = new HashMap<String, ArrayList<String>>();
        // имена коллекций, возвращаемых в результате расширенного поиска
        HashMap<String, String> targetCollectionNames = new HashMap<String, String>();

        extendedSearchPluginData = new ExtendedSearchPluginData();
        Collection<SearchAreaConfig> searchAreaConfigs = configurationService.getConfigs(SearchAreaConfig.class);

        for(SearchAreaConfig searchAreaConfig  : searchAreaConfigs){
            List<TargetDomainObjectConfig> targetObjects = searchAreaConfig.getTargetObjects();
            // список целевых ДО в конкретной области поиска
            ArrayList<String> arrayTargetObjects = new ArrayList<String>();
            for (TargetDomainObjectConfig t : targetObjects) {
                // получаем результирующую форму поиска(удаляем несоответствующие поля)
                List<IndexedFieldConfig> fields = t.getFields();
                ArrayList <String> fieldNames = new ArrayList<String>(fields.size());
                for (Iterator<IndexedFieldConfig> j = fields.iterator(); j.hasNext();)
                    fieldNames.add(j.next().getName());
                searchFields.put(t.getType(), fieldNames);
                arrayTargetObjects.add(t.getType());

                targetCollectionNames.put(t.getType(), t.getCollectionConfig().getName());
            }

            searchAreas.put(searchAreaConfig.getName(), arrayTargetObjects);
        }
        extendedSearchPluginData.setTargetCollectionNames(targetCollectionNames);
        extendedSearchPluginData.setSearchAreasData(searchAreas);
        extendedSearchPluginData.setSearchFieldsData(searchFields);

        return extendedSearchPluginData;
    }

    public HashSet<String> selectSearchFormFields (String targetDomainObject) {
        // набор полей результирующей формы для поиска
        HashSet<String> searchFormFields = new HashSet<String>();
        List<ArrayList<String>> tempDataList = new ArrayList<ArrayList<String>>();
        HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> searchFieldsData = new HashMap<String, ArrayList<String>>();
        // области поиска
        searchAreasData = extendedSearchPluginData.getSearchAreasData();
        // поля для поиска
        searchFieldsData = extendedSearchPluginData.getSearchFieldsData();
        // пробегаем по областям поиска
        for (String key : searchAreasData.keySet()) {
            // список доменных объектов в данной области
            ArrayList<String> domainObjects = searchAreasData.get(key);
            // если целевой доменный объект содержится в данной области поиска
            if (domainObjects.contains(targetDomainObject)) {
                // берем список его полей
                tempDataList.add(searchFieldsData.get(targetDomainObject));
            }
        }

        int largestSize = 0;
        // наибольший список
        ArrayList<String> biggest = new ArrayList<String>();
        // пробегаем списки полей, чтобы найти наибольший список
        for (ArrayList<String> item : tempDataList) {
             if (item.size() > largestSize) {
                 largestSize = item.size();
                 biggest = item;
             }
        }

        for (ArrayList<String> item : tempDataList) {
             for (String field : biggest) {
                 if (item.contains(field)) {
                     searchFormFields.add(field);
                 }
             }
        }

        return searchFormFields;
    }

    // обработка условий расширенного поиска и формирование результирующих данных
    public  Dto searchFormDataProcessor (Dto dto) {
        ExtendedSearchData extendedSearchData = (ExtendedSearchData) dto;
//        DomainObject domainObject = crudService.createDomainObject(extendedSearchData.getSearchQuery().getTargetObjectType());
        FormConfig formConfig = formResolver.findSearchForm(extendedSearchData.getSearchQuery().getTargetObjectType());
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        // данные из полей формы поиска
        Map<String, WidgetState> formWidgetsData = extendedSearchData.getFormWidgetsData();
        ArrayList<Id> idsWidgetObjects = null;
        Map<String, WidgetConfig> widgetConfigById = new HashMap<>();
        for (WidgetConfig config : widgetConfigs) {
            widgetConfigById.put(config.getId(), config);
        }
        // проходим по полям формы поиска, собираем данные и строим поисковые фильтры
        for (String key : formWidgetsData.keySet()) {
            // список идентификаторов объектов в виджете
            if (idsWidgetObjects == null)
            idsWidgetObjects = new ArrayList<Id>();
            WidgetState widgetState = formWidgetsData.get(key);
            // состояние меток и гиперссылок для поиска не важны
            if (widgetState instanceof LabelState) {
                continue;
            }
            if (widgetState instanceof LinkedDomainObjectHyperlinkState) {
                continue;
            }

            // для создания поисковых фильтров нужны Id доменных объектов - получаем их
            if (widgetState instanceof LinkEditingWidgetState) {
                idsWidgetObjects = ((LinkEditingWidgetState) widgetState).getIds();
            }

            // получить значение поля формы поиска можно из хэндлера виджета
            WidgetHandler widgetHandler = (WidgetHandler) applicationContext.getBean(widgetConfigById.get(key).getComponentName());
            Value value = widgetHandler.getValue(widgetState);
            try {
                Object plainValue = value.get();

                if (plainValue != null) {
                    // создание фильтров для поиска на основе виджетов формы
                    if (widgetState instanceof TextState)
                        extendedSearchData.getSearchQuery().addFilter(new TextSearchFilter(
                                          widgetConfigById.get(key).getFieldPathConfig().getValue(), value.toString()));

                    if (widgetState instanceof LinkEditingWidgetState) {
                        if (!idsWidgetObjects.isEmpty()) {
                            OneOfListFilter filter = new OneOfListFilter(widgetConfigById.get(key).getFieldPathConfig().getValue());
                            for(Id formWidgetId : idsWidgetObjects){
                                filter.addValue(formWidgetId);
                            }
                            extendedSearchData.getSearchQuery().addFilter(filter);
                        } else {
                            continue;
                        }
                        idsWidgetObjects.clear();
                    }
                }

                if (plainValue == null)
                    continue;

            } catch (NullPointerException npe) { continue; }
        }

        CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");

        ArrayList<CollectionRowItem> searchResultRowItems = new ArrayList<CollectionRowItem>();

        IdentifiableObjectCollection collection = extendedSearch(extendedSearchData);

            for (IdentifiableObject identifiableObject : collection) {
                ArrayList<String> fieldsInCollection = new ArrayList<String>();
                fieldsInCollection = identifiableObject.getFields();
                searchResultRowItems.add(collectionPluginHandler.generateCollectionRowItem(identifiableObject,
                                                                   new HashSet<String>(fieldsInCollection) ));
            }

        CollectionPluginData collectionPluginData = collectionPluginHandler.getExtendedCollectionPluginData(
                extendedSearchData.getTargetCollectionNames().get(extendedSearchData.getSearchQuery().getTargetObjectType()),
                                                                  searchResultRowItems);

        // устанавливаем имя результирующей коллекции
        collectionPluginData.setCollectionName(extendedSearchData.getTargetCollectionNames().
                get(extendedSearchData.getSearchQuery().getTargetObjectType()));

        ArrayList<CollectionRowItem> items = collectionPluginData.getItems();

        final FormPluginConfig formPluginConfig;
        if (items == null || items.isEmpty()) {
            formPluginConfig = new FormPluginConfig(extendedSearchData.getSearchQuery().getTargetObjectType());
        } else {
            formPluginConfig = new FormPluginConfig(items.get(0).getId());
            final ArrayList<Integer> selectedIndexes = new ArrayList<>(1);
            selectedIndexes.add(Integer.valueOf(0));
            collectionPluginData.setIndexesOfSelectedItems(selectedIndexes);
        }

        formPluginConfig.setDomainObjectTypeToCreate(extendedSearchData.getSearchQuery().getTargetObjectType());

        FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
//        List<ActionContext> actionContexts = formPluginHandler.initialize(formPluginConfig).getActionContexts();

        // нужно получить инициализацию формы поиска
        //ExtendedSearchFormPluginHandler extendedSearchFormPluginHandler = (ExtendedSearchFormPluginHandler)
        //                                                      applicationContext.getBean("extended.search.form.plugin");

        FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        //FormPluginData formPluginData = (FormPluginData)extendedSearchFormPluginHandler.initialize(extendedSearchData/*formPluginConfig*/);
        formPluginData.setPluginState(formPluginConfig.getPluginState());
//        formPluginData.setActionContexts(actionContexts);

        DomainObjectSurferPluginData result = new DomainObjectSurferPluginData();
        DomainObjectSurferConfig domainObjectSurferConfig = new DomainObjectSurferConfig();

        CollectionRefConfig refConfig = new CollectionRefConfig();
        refConfig.setName(extendedSearchData.getTargetCollectionNames().get(extendedSearchData.getSearchQuery().
                                                                                                getTargetObjectType()));

        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        collectionViewerConfig.setCollectionRefConfig(refConfig);

        domainObjectSurferConfig.setCollectionViewerConfig(collectionViewerConfig);

        result.setDomainObjectSurferConfig(domainObjectSurferConfig);
        result.setCollectionPluginData(collectionPluginData);
        result.setFormPluginData(formPluginData);
        result.setActionContexts(formPluginData.getActionContexts()/*actionContexts*/);
        final DomainObjectSurferPluginState dosState = new DomainObjectSurferPluginState();
        dosState.setToggleEdit(true);
        result.setPluginState(dosState);
        return result;
    }

    // запрос на поиск
    public IdentifiableObjectCollection extendedSearch(ExtendedSearchData extendedSearchData) {
        try {
            return searchService.search(extendedSearchData.getSearchQuery(),
                             extendedSearchData.getTargetCollectionNames().
                             get(extendedSearchData.getSearchQuery().getTargetObjectType()), extendedSearchData.getMaxResults());
        } catch (Exception ge) {
            throw new GuiException("Ошибка при поиске: \n" + ge.getMessage());
        }
    }
}
