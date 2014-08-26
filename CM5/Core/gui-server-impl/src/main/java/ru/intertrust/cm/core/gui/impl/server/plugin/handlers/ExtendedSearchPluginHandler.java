package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.DatePeriodFilter;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ImagePathValue;
import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.impl.server.plugin.DefaultImageMapperImpl;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectHyperlinkState;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.ValueEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginState;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

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
    DefaultImageMapperImpl defaultImageMapper;

    //@Autowired
    //private CrudService crudService;
    @Autowired
    private GuiService guiService;
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
                // если форма поиска для данного ДО не сконфигурирована, в интерфейсе не отображается
                final UserInfo userInfo = GuiContext.get().getUserInfo();
                FormDisplayData form = guiService.getSearchForm(t.getType(), new HashSet<String>(fieldNames), userInfo);
                if (form == null)
                    continue;
                arrayTargetObjects.add(t.getType());

                targetCollectionNames.put(t.getType(), t.getCollectionConfig().getName());
            }
            // если у области поиска нет сконфигурированной формы для поиска ДО, ее не отображаем
            if (arrayTargetObjects.isEmpty())
                continue;
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
        FormConfig formConfig = guiService.getFormConfig(extendedSearchData.getSearchQuery().getTargetObjectType(), FormConfig.TYPE_SEARCH);
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        // данные из полей формы поиска
        Map<String, WidgetState> formWidgetsData = extendedSearchData.getFormWidgetsData();
        ArrayList<Id> idsWidgetObjects = null;
        // список значений дат в интервале
        ArrayList<Date> dateBoxList = null;

        Map<String, WidgetState> dateBoxWidgetsDataByIds = new HashMap<>();

        // фильтр для интервалов дат
        DatePeriodFilter datePeriodFilter = null;

        //boolean isRangeDateFilterFull = false;
        Map<String, WidgetConfig> widgetConfigById = new HashMap<>();

        for (WidgetConfig config : widgetConfigs) {
            widgetConfigById.put(config.getId(), config);
        }

        for (String key : formWidgetsData.keySet()) {
            WidgetState widgetState = formWidgetsData.get(key);
            if (widgetState instanceof ValueEditingWidgetState) {
                dateBoxWidgetsDataByIds.put(key, widgetState);
            }
        }

        // Если на форме поиска 2 или более DateBox-ов, то создаем поисковый фильтр
        if (dateBoxWidgetsDataByIds.size() >= 2) {
            if (datePeriodFilter == null) {
                dateBoxList = new ArrayList<>();
                for (String k : dateBoxWidgetsDataByIds.keySet()) {
                    datePeriodFilter = new DatePeriodFilter(widgetConfigById.get(k).getFieldPathConfig().getValue());
                    break;
                }
            }
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

                    //if (widgetState instanceof ValueEditingWidgetState)
                    if (widgetState instanceof DateBoxState) {
                        // дату добавляем в список дат интервала формы поиска
                        if (dateBoxList != null)
                            dateBoxList.add((Date)plainValue);

                        /*if (((DateBoxConfig)widgetConfigById.get(key)).getRangeEndConfig().getWidgetId() != null ) {
                            if (datePeriodFilter.getEndDate() == null) {
                                datePeriodFilter.setEndDate((Date)plainValue);
                                continue;
                            }
                        }
                        if (((DateBoxConfig)widgetConfigById.get(key)).getRangeStartConfig().getWidgetId() != null ) {
                            datePeriodFilter.setStartDate((Date)plainValue);
                            continue;
                        }*/
                    }
                }
            } catch (NullPointerException npe) { continue; }
        }

        // фильтр по интервалу дат в поисковый запрос
        if (datePeriodFilter != null && dateBoxList.size() == 2) {
            extendedSearchData.getSearchQuery().addFilter(createDatePeriodFilter(datePeriodFilter, dateBoxList));
        }

        CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");

        ArrayList<CollectionRowItem> searchResultRowItems = new ArrayList<CollectionRowItem>();

        final String targetCollectionName = extendedSearchData.getTargetCollectionNames()
                .get(extendedSearchData.getSearchQuery().getTargetObjectType());
        final Collection<CollectionViewConfig> viewConfigs = configurationService.getConfigs(CollectionViewConfig.class);
        CollectionViewConfig targetViewConfig = null;
        for (CollectionViewConfig viewConfig : viewConfigs) {
            if (viewConfig.getCollection().equals(targetCollectionName) && viewConfig.isDefault()) {
                targetViewConfig = viewConfig;
                break;
            }
        }
        final LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap = new LinkedHashMap<>();
        for (CollectionColumnConfig ccConfig : targetViewConfig.getCollectionDisplayConfig().getColumnConfig()) {
            final CollectionColumnProperties properties =
                    GuiServerHelper.collectionColumnConfigToProperties(ccConfig, null, null);
            columnPropertiesMap.put(ccConfig.getField(), properties);
        }
        IdentifiableObjectCollection collection = extendedSearch(extendedSearchData);
        for (IdentifiableObject identifiableObject : collection) {
            final Map<String, Map<Value, ImagePathValue>> fieldMaps = defaultImageMapper.getImageMaps(columnPropertiesMap);
            searchResultRowItems.add(collectionPluginHandler.generateCollectionRowItem(
                            identifiableObject, columnPropertiesMap, fieldMaps));
        }
        final CollectionPluginData collectionPluginData = collectionPluginHandler
                .getExtendedCollectionPluginData(targetCollectionName, "ext-search", searchResultRowItems);
        collectionPluginData.setExtendedSearchMarker(true);
        collectionPluginData.setCollectionViewConfigName(targetViewConfig.getName());
        collectionPluginData.setDomainObjectFieldPropertiesMap(columnPropertiesMap);
        collectionPluginData.setCollectionName(targetCollectionName);
        ArrayList<CollectionRowItem> items = collectionPluginData.getItems();
        final FormPluginConfig formPluginConfig;
        if (items == null || items.isEmpty()) {
            formPluginConfig = new FormPluginConfig(extendedSearchData.getSearchQuery().getTargetObjectType());
        } else {
            final Id selectedId = items.get(0).getId();
            formPluginConfig = new FormPluginConfig(selectedId);
            final ArrayList<Id> selectedIds = new ArrayList<>(1);
            selectedIds.add(selectedId);
            collectionPluginData.setChosenIds(selectedIds);
        }
        final FormPluginState fpState = new FormPluginState();
        fpState.setInCentralPanel(false);
        fpState.setEditable(false);
        fpState.setToggleEdit(true);
        formPluginConfig.setPluginState(fpState);

        FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        final ToolbarContext toolbarContext = formPluginHandler.initialize(formPluginConfig).getToolbarContext();

        // нужно получить инициализацию формы поиска
        //ExtendedSearchFormPluginHandler extendedSearchFormPluginHandler = (ExtendedSearchFormPluginHandler)
        //                                                      applicationContext.getBean("extended.search.form.plugin");

        FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        formPluginData.setPluginState(formPluginConfig.getPluginState());
        formPluginData.setToolbarContext(toolbarContext);

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
        final DomainObjectSurferPluginState dosState = new DomainObjectSurferPluginState();
        dosState.setToggleEdit(true);
        result.setPluginState(dosState);
        return result;
    }

    private DatePeriodFilter createDatePeriodFilter(DatePeriodFilter datePeriodFilter, ArrayList<Date> dates) {
        //DatePeriodFilter dpf = new DatePeriodFilter();
        //dpf = datePeriodFilter;
        Date date1 = dates.get(0);
        Date date2 = dates.get(1);

        if (date1.after(date2)) {
            datePeriodFilter.setEndDate(date1);
        }
        if (date2.before(date1)) {
            datePeriodFilter.setStartDate(date2);
        }

        return datePeriodFilter;
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
