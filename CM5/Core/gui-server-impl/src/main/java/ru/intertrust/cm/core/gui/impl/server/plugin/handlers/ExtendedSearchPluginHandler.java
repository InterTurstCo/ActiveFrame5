package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.DateBoxConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageKey;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.SearchAreaConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.DefaultImageMapperImpl;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.plugin.*;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.ExtendedSearchCollectionPluginData;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.intertrust.cm.core.business.api.dto.FieldType.*;

/**
 * User: IPetrov Date: 03.01.14 Time: 15:58 Обработчик плагина расширенного
 * поиска получает данные о конфигурации поиска и вызывает сервис поиска
 */
@ComponentName("extended.search.plugin")
public class ExtendedSearchPluginHandler extends PluginHandler {

    SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    ConfigurationExplorer configurationService;

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    SearchService searchService;

    @Autowired
    DefaultImageMapperImpl defaultImageMapper;

    @Autowired
    private GuiService guiService;

    protected ExtendedSearchPluginData extendedSearchPluginData;

    @Override
    public ExtendedSearchPluginData initialize(Dto params) {
        // область поиска - список целевых ДО
        HashMap<String, ArrayList<String>> searchAreas = new HashMap<String, ArrayList<String>>();
        // целевой ДО - список его полей
        HashMap<String, ArrayList<String>> searchFields = new HashMap<String, ArrayList<String>>();
        // имена коллекций, возвращаемых в результате расширенного поиска
        HashMap<String, String> targetCollectionNames = new HashMap<String, String>();

        extendedSearchPluginData = new ExtendedSearchPluginData();
        Collection<SearchAreaConfig> searchAreaConfigs = configurationService.getConfigs(SearchAreaConfig.class);

        for (SearchAreaConfig searchAreaConfig : searchAreaConfigs) {
            List<TargetDomainObjectConfig> targetObjects = searchAreaConfig.getTargetObjects();
            // список целевых ДО в конкретной области поиска
            ArrayList<String> arrayTargetObjects = new ArrayList<String>();
            for (TargetDomainObjectConfig targetObject : targetObjects) {
                // получаем результирующую форму поиска(удаляем
                // несоответствующие поля)
                List<IndexedFieldConfig> fields = targetObject.getFields();
                ArrayList<String> fieldNames = new ArrayList<String>(fields.size());
                for (IndexedFieldConfig field : fields) {
                    fieldNames.add(field.getName());
                }
                searchFields.put(targetObject.getType(), fieldNames);
                // если форма поиска для данного ДО не сконфигурирована, в
                // интерфейсе не отображается
                final UserInfo userInfo = GuiContext.get().getUserInfo();
                FormDisplayData form = guiService.getSearchForm(targetObject.getType(), new HashSet<String>(fieldNames), userInfo);
                if (form != null) {
                    arrayTargetObjects.add(targetObject.getType());
                    targetCollectionNames.put(targetObject.getType(), targetObject.getCollectionConfig().getName());
                }
            }
            // если у области поиска нет сконфигурированной формы для поиска ДО,
            // ее не отображаем
            if (!arrayTargetObjects.isEmpty()) {
                searchAreas.put(searchAreaConfig.getName(), arrayTargetObjects);
            }
        }
        Collection<BusinessUniverseConfig> businessUniverseConfigs = configurationService
                .getConfigs(BusinessUniverseConfig.class);
        if (!businessUniverseConfigs.isEmpty()) {
            extendedSearchPluginData.setExtendedSearchPopupConfig(businessUniverseConfigs.iterator().next().getExtendedSearchPopupConfig());
        }
        extendedSearchPluginData.setTargetCollectionNames(targetCollectionNames);
        extendedSearchPluginData.setSearchAreasData(searchAreas);
        extendedSearchPluginData.setSearchFieldsData(searchFields);
        Map<String, String> valueToDisplayText = getLocalizationMap(searchAreas, searchFields);
        extendedSearchPluginData.setValueToDisplayText(valueToDisplayText);
        return extendedSearchPluginData;
    }

    private Map<String, String> getLocalizationMap(Map<String, ArrayList<String>> searchAreasData, Map<String, ArrayList<String>> searchFieldsData) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> areaData : searchAreasData.entrySet()) {
            String searchAreaName = areaData.getKey();
            result.put(searchAreaName, MessageResourceProvider.getMessage(new MessageKey(searchAreaName,
                    MessageResourceProvider.SEARCH_AREA), GuiContext.getUserLocale()));
            List<String> domainObjectTypes = areaData.getValue();
            for (String doType : domainObjectTypes) {
                result.put(doType, MessageResourceProvider.getMessage(new MessageKey(doType, MessageResourceProvider
                        .SEARCH_DOMAIN_OBJECT), GuiContext.getUserLocale()));
            }
        }
        for (Map.Entry<String, ArrayList<String>> fieldsData : searchFieldsData.entrySet()) {
            String doType = fieldsData.getKey();
            for (String field : fieldsData.getValue()) {
                Map<String, String> context = new HashMap<>();
                context.put(MessageResourceProvider.DOMAIN_OBJECT_CONTEXT, doType);
                result.put(field, MessageResourceProvider.getMessage(new MessageKey(field, MessageResourceProvider
                        .SEARCH_FIELD, context), GuiContext.getUserLocale()));
            }
        }

        return result;
    }

    public HashSet<String> selectSearchFormFields(String targetDomainObject) {
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

    // обработка условий расширенного поиска и формирование результирующих
    // данных
    public Dto searchFormDataProcessor(Dto dto) {
        ExtendedSearchData extendedSearchData = (ExtendedSearchData) dto;
        FormConfig formConfig = guiService.getFormConfig(extendedSearchData.getSearchQuery().getTargetObjectTypes().get(0),
                FormConfig.TYPE_SEARCH);
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();

        Map<String, WidgetConfig> widgetConfigById = new HashMap<>();
        for (WidgetConfig config : widgetConfigs) {
            widgetConfigById.put(config.getId(), config);
        }
        SearchQuery searchQuery = extendedSearchData.getSearchQuery();

        // данные из полей формы поиска
        Map<String, WidgetState> formWidgetsData = extendedSearchData.getFormWidgetsData();
        // Кэш диапазонных фильтров - чтобы не искать их в поисковом запросе при
        // обработке второго виджета
        Map<String, TimeIntervalFilter> rangeFilters = new HashMap<>();

        Map<String, String> formWidgetStringData = new HashMap<>();


        // проходим по полям формы поиска, собираем данные и строим поисковые
        // фильтры
        for (Map.Entry<String, WidgetState> entry : formWidgetsData.entrySet()) {
            String widgetId = entry.getKey();
            WidgetState widgetState = entry.getValue();// formWidgetsData.get(key);
            WidgetConfig widgetConfig = widgetConfigById.get(widgetId);
            String fieldPath = widgetConfig.getFieldPathConfig().getValue();

            if (widgetState instanceof LinkEditingWidgetState) {

                LinkedHashMap<Id, String> titleMap = new LinkedHashMap<>();

                if(widgetState instanceof ListWidgetState){
                    titleMap = ((ListWidgetState) widgetState).getListValues();
                };

                List<Id> ids = ((LinkEditingWidgetState)widgetState).getIds();

                if (ids != null && !ids.isEmpty()) {
                    OneOfListFilter filter = new OneOfListFilter(fieldPath);
                    StringBuilder sBuilder = new StringBuilder();
                    for (Id id : ids) {
                        if(sBuilder.length() > 0){
                            sBuilder.append(", ");
                        }
                        sBuilder.append(titleMap.get(id));
                        filter.addValue(id);
                    }
                    searchQuery.addFilter(filter);
                    formWidgetStringData.put(fieldPath, sBuilder.toString());
                }
            } else if (widgetState instanceof TextState) {
                String text = ((TextState) widgetState).getText();
                if (text != null && !text.isEmpty()) {
                    TextSearchFilter filter = new TextSearchFilter(fieldPath, text);
                    searchQuery.addFilter(filter);
                    formWidgetStringData.put(fieldPath, text);
                }
            } else if (widgetState instanceof DateBoxState) {
                WidgetHandler widgetHandler = PluginHandlerHelper.getWidgetHandler(widgetConfig, applicationContext);
                Value<?> value = widgetHandler.getValue(widgetState);
                if (value != null && value.get() != null) {
                    TimeIntervalFilter filter;
                    if (!rangeFilters.containsKey(fieldPath)) {
                        filter = new TimeIntervalFilter(fieldPath);
                        rangeFilters.put(fieldPath, filter);
                        searchQuery.addFilter(filter);
                    } else {
                        filter = rangeFilters.get(fieldPath);
                    }
                    DateBoxConfig dateBoxConfig = (DateBoxConfig) widgetConfig;
                    if (dateBoxConfig.getRangeEndConfig() != null) {
                        // Есть ссылка на виджет конца диапазона - значит,
                        // выбранный виджет задаёт начало
                        initStartDate(filter, value);
                        String startDate = getDateTimeStringValue(value);
                        if(!formWidgetStringData.containsKey(fieldPath)){
                            formWidgetStringData.put(fieldPath, startDate);
                        }else{
                            String endDate =  formWidgetStringData.get(fieldPath);
                            formWidgetStringData.put(fieldPath, "C " + startDate + " по " + endDate);
                        }
                    } else if (dateBoxConfig.getRangeStartConfig() != null) {
                        initEndDate(filter, value);

                        String endDate = getDateTimeStringValue(value);
                        if(!formWidgetStringData.containsKey(fieldPath)){
                            formWidgetStringData.put(fieldPath, endDate);
                        }else{
                            String startDate =  formWidgetStringData.get(fieldPath);
                            formWidgetStringData.put(fieldPath, "C " + startDate + " по " + endDate);
                        }


                    } else {
                        // Виджет не связан по диапазону - ищем по фиксированной
                        // дате
                        initEndDate(filter, value);
                       formWidgetStringData.put(fieldPath, getDateTimeStringValue(value));
                    }

                }
            } else if (widgetState instanceof CheckBoxState) {
                boolean value = ((CheckBoxState) widgetState).isSelected();
                BooleanSearchFilter filter = new BooleanSearchFilter(fieldPath, value);
                searchQuery.addFilter(filter);
                formWidgetStringData.put(fieldPath, value ? "Да" : "Нет");
            } else if (widgetState instanceof EnumBoxState) {
                EnumBoxState state = (EnumBoxState) widgetState;
                Value<?> value = state.getDisplayTextToValue().get(state.getSelectedText());
                formWidgetStringData.put(fieldPath, state.getSelectedText());
                searchQuery.addFilter(buildFilterForEnumBox(fieldPath, value));
            }
        }

        CollectionPluginHandler collectionPluginHandler =
                (CollectionPluginHandler) applicationContext.getBean("collection.plugin");

        ArrayList<CollectionRowItem> searchResultRowItems = new ArrayList<CollectionRowItem>();

        final String targetCollectionName = extendedSearchData.getTargetCollectionNames()
                .get(extendedSearchData.getSearchQuery().getTargetObjectTypes().get(0));
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
                    identifiableObject, columnPropertiesMap, fieldMaps, false));
        }
        final ExtendedSearchCollectionPluginData collectionPluginData = collectionPluginHandler
                .getExtendedCollectionPluginData(targetCollectionName, "ext-search", searchResultRowItems);
        collectionPluginData.setExtendedSearchMarker(true);
        collectionPluginData.setCollectionViewConfigName(targetViewConfig.getName());
        collectionPluginData.setDomainObjectFieldPropertiesMap(columnPropertiesMap);
        collectionPluginData.setCollectionName(targetCollectionName);
        collectionPluginData.setSearchQuery(extendedSearchData.getSearchQuery());
        collectionPluginData.setRowsChunk(WidgetConstants.UNBOUNDED_LIMIT);
        collectionPluginData.setSearchArea("");
        ArrayList<CollectionRowItem> items = collectionPluginData.getItems();
        final FormPluginConfig formPluginConfig;
        if (items == null || items.isEmpty()) {
            formPluginConfig = new FormPluginConfig(extendedSearchData.getSearchQuery().getTargetObjectTypes().get(0));
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
        //final ToolbarContext toolbarContext = formPluginHandler.initialize(formPluginConfig).getToolbarContext();

        // нужно получить инициализацию формы поиска
        // ExtendedSearchFormPluginHandler extendedSearchFormPluginHandler =
        // (ExtendedSearchFormPluginHandler)
        // applicationContext.getBean("extended.search.form.plugin");

        FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        formPluginData.setPluginState(formPluginConfig.getPluginState());
        //formPluginData.setToolbarContext(toolbarContext);

        ExtendedSearchDomainObjectSurfacePluginData result = new ExtendedSearchDomainObjectSurfacePluginData();
        DomainObjectSurferConfig domainObjectSurferConfig = new DomainObjectSurferConfig();

        CollectionRefConfig refConfig = new CollectionRefConfig();
        refConfig.setName(extendedSearchData.getTargetCollectionNames().get(extendedSearchData.getSearchQuery().
                getTargetObjectTypes().get(0)));

        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        collectionViewerConfig.setCollectionRefConfig(refConfig);

        domainObjectSurferConfig.setCollectionViewerConfig(collectionViewerConfig);

        result.setExtendedSearchConfiguration(formWidgetsData);
        result.setSearchAreas(searchQuery.getAreas());

        result.setSearchDomainObjectType(searchQuery.getTargetObjectTypes().get(0));
        result.setDomainObjectSurferConfig(domainObjectSurferConfig);
        result.setCollectionPluginData(collectionPluginData);
        result.setFormPluginData(formPluginData);


        result.getInfoBarContext().addInfoBarItem( MessageResourceProvider.getMessage(MessageResourceProvider.SEARCH_AREA_RESULT , GuiContext.getUserLocale()) + " : ", String.valueOf(collectionPluginData.getItems().size()));
        StringBuilder filterValueBuilder = new StringBuilder();
        Map<String, String> localDataMap = getSearchAreaFieldsConfig();
        for(Map.Entry<String, String> widgetEntryValue : formWidgetStringData.entrySet()){
            if(filterValueBuilder.length() > 0){
                filterValueBuilder.append("; ");
            }
            String fieldName = localDataMap.get(widgetEntryValue.getKey());

            filterValueBuilder.append(fieldName).append(" : ").append(widgetEntryValue.getValue());
        }

        result.getInfoBarContext().addInfoBarItem(MessageResourceProvider.getMessage(MessageResourceProvider.SEARCH_AREA_FILERTS, GuiContext.getUserLocale()) + " : ", filterValueBuilder.toString());
        final DomainObjectSurferPluginState dosState = new DomainObjectSurferPluginState();
        dosState.setToggleEdit(true);
        result.setPluginState(dosState);
        return result;
    }

    private  Map<String, String> getSearchAreaFieldsConfig(){

        // область поиска - список целевых ДО
        HashMap<String, ArrayList<String>> searchAreas = new HashMap<String, ArrayList<String>>();
        // целевой ДО - список его полей
        HashMap<String, ArrayList<String>> searchFields = new HashMap<String, ArrayList<String>>();

        Collection<SearchAreaConfig> searchAreaConfigs = configurationService.getConfigs(SearchAreaConfig.class);

        for (SearchAreaConfig searchAreaConfig : searchAreaConfigs) {
            List<TargetDomainObjectConfig> targetObjects = searchAreaConfig.getTargetObjects();
            // список целевых ДО в конкретной области поиска
            ArrayList<String> arrayTargetObjects = new ArrayList<String>();
            for (TargetDomainObjectConfig targetObject : targetObjects) {
                // получаем результирующую форму поиска(удаляем
                // несоответствующие поля)
                List<IndexedFieldConfig> fields = targetObject.getFields();
                ArrayList<String> fieldNames = new ArrayList<String>(fields.size());
                for (IndexedFieldConfig field : fields) {
                    fieldNames.add(field.getName());
                }
                searchFields.put(targetObject.getType(), fieldNames);
                // если форма поиска для данного ДО не сконфигурирована, в
                // интерфейсе не отображается
                final UserInfo userInfo = GuiContext.get().getUserInfo();
                FormDisplayData form = guiService.getSearchForm(targetObject.getType(), new HashSet<String>(fieldNames), userInfo);
                if (form != null) {
                    arrayTargetObjects.add(targetObject.getType());

                }
            }
            // если у области поиска нет сконфигурированной формы для поиска ДО,
            // ее не отображаем
            if (!arrayTargetObjects.isEmpty()) {
                searchAreas.put(searchAreaConfig.getName(), arrayTargetObjects);
            }
        }
        return getLocalizationMap(searchAreas, searchFields);
    }

    private String getFieldName(SearchQuery searchQuery, String fieldPath, Map<String, String> localDataMap) {

        return fieldPath;
    }


    protected SearchFilter buildFilterForEnumBox(String fieldPath, Value<?> value) {
        FieldType type = value.getFieldType();
        if (type.equals(LONG)) {
            long v = (Long) value.get();
            NumberRangeFilter filter = new NumberRangeFilter(fieldPath);
            filter.setMin(v);
            filter.setMax(v);
            return filter;
        } else if (type.equals(DECIMAL)) {
            BigDecimal v = (BigDecimal) value.get();
            NumberRangeFilter filter = new NumberRangeFilter(fieldPath);
            filter.setMin(v);
            filter.setMax(v);
            return filter;
        } else if (type.equals(BOOLEAN)) {
            return new BooleanSearchFilter(fieldPath, (Boolean) value.get());
        } else if (type.equals(DATETIMEWITHTIMEZONE)) {
            DateTimeWithTimeZone v = (DateTimeWithTimeZone) value.get();
            Date dateValue = convert(v);
            return new TimeIntervalFilter(fieldPath, dateValue , dateValue);
        } else if (type.equals(TIMELESSDATE)) {
            TimelessDate v = (TimelessDate) value.get();
            Date dateValue = v != null ? v.toDate() : null;
            return new TimeIntervalFilter(fieldPath, dateValue, dateValue);
        } else if (type.equals(DATETIME)) {
            Date v = (Date) value.get();
            return new TimeIntervalFilter(fieldPath, v, v);
        } else if (type.equals(REFERENCE)) {
            return new OneOfListFilter(fieldPath, (Id) value.get());
        } else if (type.equals(STRING)) {
            return new TextSearchFilter(fieldPath, (String) value.get());
        } else {
            return new EmptyValueFilter(fieldPath);
        }
    }

    private Date convert(DateTimeWithTimeZone source) {
        TimeZone tz = TimeZone.getTimeZone(source.getTimeZoneContext().getTimeZoneId());
        Calendar cal = Calendar.getInstance(tz);
        cal.set(source.getYear(), source.getMonth(), source.getDayOfMonth(),
                source.getHours(), source.getMinutes(), source.getSeconds());
        cal.set(Calendar.MILLISECOND, source.getMilliseconds());
        return cal.getTime();
    }

    private void initStartDate(TimeIntervalFilter filter, Value<?> date) {
        if (date instanceof DateTimeValue) {
            filter.setStartTime(((DateTimeValue) date).get());
        } else if (date instanceof TimelessDateValue) {
            TimelessDate tdValue = ((TimelessDateValue) date).get();
            Date dateValue = tdValue != null ? tdValue.toDate() : null;

            filter.setStartTime(dateValue);
        } else if (date instanceof DateTimeWithTimeZoneValue) {
            DateTimeWithTimeZone dtwzValue = ((DateTimeWithTimeZoneValue)date).get();

            filter.setStartTime(convert(dtwzValue));
        } else {
            throw new IllegalArgumentException("Unexpected value type: " + date.getFieldType());
        }
    }

    private String getDateTimeStringValue(Value<?> date) {
        if (date instanceof DateTimeValue) {
            return formater.format(((DateTimeValue) date).get());
        } else if (date instanceof TimelessDateValue) {
            return formater.format(((TimelessDateValue) date).get());
        } else if (date instanceof DateTimeWithTimeZoneValue) {
            return formater.format(((DateTimeWithTimeZoneValue) date).get());
        } else {
            throw new IllegalArgumentException("Unexpected value type: " + date.getFieldType());
        }
    }

    private void initEndDate(TimeIntervalFilter filter, Value<?> date) {
        if (date instanceof DateTimeValue) {
            filter.setEndTime(((DateTimeValue) date).get());
        } else if (date instanceof TimelessDateValue) {
            TimelessDate tdValue = ((TimelessDateValue) date).get();
            Date dateValue = tdValue != null ? tdValue.toDate() : null;

            filter.setEndTime(dateValue);
        } else if (date instanceof DateTimeWithTimeZoneValue) {
            DateTimeWithTimeZone dtwzValue = ((DateTimeWithTimeZoneValue)date).get();
            filter.setEndTime(convert(dtwzValue));
        } else {
            throw new IllegalArgumentException("Unexpected value type: " + date.getFieldType());
        }
    }

    // запрос на поиск
    public IdentifiableObjectCollection extendedSearch(ExtendedSearchData extendedSearchData) {
        try {
            return searchService.search(extendedSearchData.getSearchQuery(),
                    extendedSearchData.getTargetCollectionNames().
                            get(extendedSearchData.getSearchQuery().getTargetObjectTypes().get(0)), extendedSearchData.getMaxResults());
        } catch (Exception ge) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_SEARCH,
                    "Ошибка при поиске:\\n",
                    GuiContext.getUserLocale()) + ge.getMessage());
        }
    }

}
