package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.gui.form.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.*;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.util.ConfigurationUtil;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.*;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
@Stateless
@EJB(name = "java:app/GuiService", beanInterface = GuiService.class)
@DeclareRoles("cm_user")
@RolesAllowed("cm_user")
@Local(GuiService.class)
@Remote(GuiService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class GuiServiceImpl implements GuiService, GuiService.Remote {

    static Logger log = LoggerFactory.getLogger(GuiServiceImpl.class);

    @Autowired
    ApplicationContext applicationContext;

    @EJB
    private ConfigurationService configurationService;

    @EJB
    private CrudService crudService;

    @EJB
    private CollectionsService collectionsService;

    @Override
    public NavigationConfig getNavigationConfiguration() {
        String navigationPanelName = "panel";
        NavigationConfig navigationConfig = configurationService.getConfig(NavigationConfig.class, navigationPanelName);
        return navigationConfig;
    }

    @Override
    public Dto executeCommand(Command command) {
        ComponentHandler pluginHandler = obtainHandler(command.getComponentName());
        if (pluginHandler == null) {
            log.warn("handler for component '{}' not found", command.getComponentName());
            return null;
        }
        try {
            return (Dto) pluginHandler.getClass().getMethod(command.getName(), Dto.class)
                    .invoke(pluginHandler, command.getParameter());
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new GuiException("No command + " + command.getName() + " implemented");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GuiException("Command can't be executed: " + command.getName());
        }
    }

    @Override
    public Form getForm(String domainObjectType) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        // todo: separate empty form?
        return buildDomainObjectForm(root);
    }

    public Form getForm(Id domainObjectId) {
        // по Id ищется тип доменного объекта
        // далее находится форма для данного контекста, учитывая факт того, переопределена ли форма для пользователя/роли,
        // если флаг "использовать по умолчанию" не установлен
        // в конечном итоге получаем FormConfig

        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        return buildDomainObjectForm(root);
    }

    public DomainObject saveForm(Form form) {
        FormConfig formConfig = configurationService.getConfig(FormConfig.class, form.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        FormData formData = form.getFormData();

        HashSet<FieldPath> objectsFieldPathsToSave = new HashSet<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetData widgetData = form.getWidgetData(widgetConfig.getId());
            if (widgetData == null) { // ignore - such data shouldn't be saved
                continue;
            }
            Value newValue = widgetData.toValue();
            FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
            Value oldValue = formData.getFieldPathValue(fieldPath);
            if (!newValue.equals(oldValue)) {
                formData.setFieldPathValue(fieldPath, newValue);
                objectsFieldPathsToSave.add(fieldPath.createFieldPathWithoutLastElement());
            }
        }
        ArrayList<DomainObject> toSave = new ArrayList<>(objectsFieldPathsToSave.size());
        // todo sort field paths in such a way that linked objects are saved first?
        // root DO is save separately as we should return it's identifier in case it's created from scratch
        boolean saveRoot = false;
        for (FieldPath fieldPath : objectsFieldPathsToSave) {
            if (fieldPath.equals(FieldPath.ROOT)) {
                saveRoot = true;
                continue;
            }
            toSave.add(formData.getFieldPathObject(fieldPath));
        }
        crudService.save(toSave);
        DomainObject rootDomainObject = formData.getFieldPathObject(FieldPath.ROOT);
        if (saveRoot) {
            return crudService.save(rootDomainObject);
        } else {
            return rootDomainObject;
        }
    }

    private Form buildDomainObjectForm(DomainObject root) {
        HashMap<String, WidgetData> widgetDataMap = new HashMap<>();
        FormConfig formConfig = findFormConfig(root);
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        FormData formData = getFormData(root, widgetConfigs);
        for (WidgetConfig config : widgetConfigs) {
            WidgetHandler componentHandler = obtainHandler(ConfigurationUtil.getWidgetTag(config));
            WidgetContext widgetContext = new WidgetContext();
            widgetContext.setWidgetConfig(config);
            widgetDataMap.put(config.getId(), componentHandler.getInitialDisplayData(widgetContext, formData));
        }
        Form form = new Form(formConfig.getName(), formConfig.getMarkup(), widgetDataMap, formData);
        return form;
    }

    private FormConfig findFormConfig(DomainObject root) {
        // todo drop HARDCODE
        String typeName = root.getTypeName();
        switch (typeName) {
            case "country":
                return configurationService.getConfig(FormConfig.class, "country_form");
            case "city":
                return configurationService.getConfig(FormConfig.class, "city_form");
            default:
                throw new GuiException("Form not found for type: " + typeName);
        }
    }

    private Map<WidgetConfig, FieldPath> getFieldPaths(List<WidgetConfig> configs) {
        HashMap<WidgetConfig, FieldPath> paths = new HashMap<>(configs.size());
        for (WidgetConfig config : configs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + config.getId() + " is not configured with Field Path");
                } else {
                    continue;
                }
            }
            paths.put(config, new FieldPath(fieldPathConfig.getValue()));
        }
        return paths;
    }

    private FormData getFormData(DomainObject root, List<WidgetConfig> widgetConfigs) {
        // не уверен, нужен ли здесь будет Business Object, но наверно нужен в некотором урезанном виде - для оптимистических блокировок

        Map<WidgetConfig, FieldPath> fieldPaths = getFieldPaths(widgetConfigs); // todo -> convert to list

        HashMap<FieldPath, DomainObject> fieldPathObjects = new HashMap<>();
        fieldPathObjects.put(new FieldPath(), root);
        for (FieldPath fieldPath : fieldPaths.values()) {
            DomainObject currentRoot = root;
            for (Iterator<FieldPath> subPathIterator = fieldPath.subPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                if (!subPathIterator.hasNext()) { // значит текущий путь указывает на Value и будет получаться из Domain Object
                    break; // значит ничего не делаем, а раз следующего нет, выходим из цикла
                }
                if (fieldPathObjects.containsKey(subPath)) {
                    continue;
                }

                String linkField = subPath.getLastElement();
                if (linkField.contains("^")) { // it's a "back-link"
                    //todo
                } else {
                    Id linkedObjectId = currentRoot.getReference(linkField);
                    if (linkedObjectId != null) {
                        DomainObject linkedDo = crudService.find(linkedObjectId);
                        fieldPathObjects.put(subPath, linkedDo);
                        currentRoot = linkedDo;
                    } else {
                        // текущий root становится null, таким образом все последующие вызовы бессмыссленны
                        break;

                        // todo или создавать пустой Domain Object? если мы разрешаем сохранение "новых" связанных
                        // объектов, то нужна для этого инфраструктура
                        // сценарий: у страны есть столица, а на форме показано название столицы. когда столица не назначена,
                        // поле пусто. Когда его заполняет пользователь, то такую столицу надо создать...
                    }
                }
            }
        }

        FormData formData = new FormData();
        formData.setFieldPathObjects(fieldPathObjects);

        return formData;
    }

    private FormConfig createFakeConfig(Map<String, WidgetData> widgetDataMapToFill) {
        MarkupConfig markup = new MarkupConfig();
        markup.setHeader(getHeaderConfig(widgetDataMapToFill));
        markup.setBody(getBodyConfig(widgetDataMapToFill));

        FormConfig config = new FormConfig();
        config.setName("some_form");
        config.setDomainObjectType("person");
        config.setWidgetConfigurationConfig(null);
        config.setMarkup(markup);

        return config;
    }

    private HeaderConfig getHeaderConfig(Map<String, WidgetData> widgetDataMap) {
        // 2 закладки и в каждой:
        // +-------------+----------------------------+------------+-------------------------+
        // |        Имя: | Текстовое поле             |   Возраст: | Целое поле              |
        // +-------------+----------------------------+------------+                         +
        // |    Фамилия: | Текстовое поле                          |                         |
        // +-------------+----------------------------+------------+-------------------------+

        WidgetDef nameLabel = WidgetDef.createLabelCell("1", "Имя:");
        WidgetDef nameTextBox = WidgetDef.createTextBoxCell("2", "Василий", "1");
        WidgetDef ageLabel = WidgetDef.createLabelCell("3", "Возраст:");
        WidgetDef ageIntegerBoxCell = WidgetDef.createIntegerBoxCell("4", 27, "1", "2");

        WidgetDef surnameLabel = WidgetDef.createLabelCell("5", "Фамилия:");
        WidgetDef surnameTextBox = WidgetDef.createTextBoxCell("6", "Длиннофамильный-Закарпатскийараратов", "1");

        addWidgetDataToMap(new WidgetDef[]{nameLabel, nameTextBox, ageLabel, ageIntegerBoxCell, surnameLabel, surnameTextBox}, widgetDataMap);

        ArrayList<CellConfig> headerRow1Cols = new ArrayList<>();
        headerRow1Cols.add(nameLabel.cellConfig);
        headerRow1Cols.add(nameTextBox.cellConfig);
        headerRow1Cols.add(ageLabel.cellConfig);
        headerRow1Cols.add(ageIntegerBoxCell.cellConfig);

        ArrayList<CellConfig> headerRow2Cols = new ArrayList<>();
        headerRow2Cols.add(surnameLabel.cellConfig);
        headerRow2Cols.add(surnameTextBox.cellConfig);

        RowConfig row1Config = new RowConfig();
        row1Config.setCells(headerRow1Cols);

        RowConfig row2Config = new RowConfig();
        row2Config.setCells(headerRow2Cols);

        ArrayList<RowConfig> headerRows = new ArrayList<>();
        headerRows.add(row1Config);
        headerRows.add(row2Config);

        TableLayoutConfig headerLayout = new TableLayoutConfig();
        headerLayout.setWidth("500px");
        headerLayout.setRows(headerRows);
        HeaderConfig header = new HeaderConfig();
        header.setTableLayout(headerLayout);
        return header;
    }

    private BodyConfig getBodyConfig(Map<String, WidgetData> widgetDataMap) {
        TabConfig tab1 = createBodyTab("Главная", widgetDataMap);
        TabConfig tab2 = createBodyTab("Второстепенная", widgetDataMap);

        ArrayList<TabConfig> tabs = new ArrayList<>();
        tabs.add(tab1);
        tabs.add(tab2);

        BodyConfig bodyConfig = new BodyConfig();
        bodyConfig.setTabs(tabs);

        return bodyConfig;
    }

    private TabConfig createBodyTab(String name, Map<String, WidgetData> form) {
        // 2 закладки и в каждой:
        // +-------------+----------------------------+------------+-------------------------+
        // |    Рост(см):| Целое поле                 | Цвет глаз: |                         |
        // +-------------+----------------------------+------------+-------------------------+
        // |     Вес(кг):| Целое поле                 |     Хобби: |                         |
        // +-------------+----------------------------+------------+-------------------------+

        WidgetDef heightLabel = WidgetDef.createLabelCell("7" + name.hashCode(), "Рост(см):");
        WidgetDef heightIntegerBox = WidgetDef.createTextBoxCell("8" + name.hashCode(), "222", "1");
        WidgetDef eyesColorLabel = WidgetDef.createLabelCell("9" + name.hashCode(), "Цвет глаз:");
        WidgetDef eyesColorTextBox = WidgetDef.createTextBoxCell("10" + name.hashCode(), "Сизый", "1");

        WidgetDef weightLabel = WidgetDef.createLabelCell("11" + name.hashCode(), "Вес(кг):");
        WidgetDef weightIntegerBox = WidgetDef.createTextBoxCell("12" + name.hashCode(), "222", "1");
        WidgetDef hobbyLabel = WidgetDef.createLabelCell("13" + name.hashCode(), "Хобби:");
        WidgetDef hobbyTextBox = WidgetDef.createTextBoxCell("14" + name.hashCode(), "Продажа слонов", "1");

        addWidgetDataToMap(
                new WidgetDef[]{heightLabel, heightIntegerBox, eyesColorLabel, eyesColorTextBox, weightLabel,
                        weightIntegerBox, hobbyLabel, hobbyTextBox}, form);

        ArrayList<CellConfig> headerRow1Cols = new ArrayList<>();
        headerRow1Cols.add(heightLabel.cellConfig);
        headerRow1Cols.add(heightIntegerBox.cellConfig);
        headerRow1Cols.add(eyesColorLabel.cellConfig);
        headerRow1Cols.add(eyesColorTextBox.cellConfig);

        ArrayList<CellConfig> headerRow2Cols = new ArrayList<>();
        headerRow2Cols.add(weightLabel.cellConfig);
        headerRow2Cols.add(weightIntegerBox.cellConfig);
        headerRow2Cols.add(hobbyLabel.cellConfig);
        headerRow2Cols.add(hobbyTextBox.cellConfig);

        RowConfig row1Config = new RowConfig();
        row1Config.setCells(headerRow1Cols);

        RowConfig row2Config = new RowConfig();
        row2Config.setCells(headerRow2Cols);

        ArrayList<RowConfig> headerRows = new ArrayList<>();
        headerRows.add(row1Config);
        headerRows.add(row2Config);

        TableLayoutConfig layout = new TableLayoutConfig();
        layout.setWidth("500px");
        layout.setRows(headerRows);

        TabGroupConfig tabGroupConfig = new TabGroupConfig();
        tabGroupConfig.setLayout(layout);

        SingleEntryGroupListConfig groupList = new SingleEntryGroupListConfig();
        groupList.setTabGroupConfig(tabGroupConfig);

        TabConfig tab = new TabConfig();
        tab.setName(name);
        tab.setGroupList(groupList);
        return tab;
    }

    private void addWidgetDataToMap(WidgetDef[] widgetDefs, Map<String, WidgetData> form) {
        for (WidgetDef widgetDef : widgetDefs) {
            form.put(widgetDef.widgetConfig.getId(), widgetDef.widgetData);
        }
    }

    private static class WidgetDef {
        public final CellConfig cellConfig;
        public final WidgetConfig widgetConfig;
        public final WidgetData widgetData;

        public WidgetDef(CellConfig cellConfig, WidgetConfig widgetConfig, WidgetData widgetData) {
            this.cellConfig = cellConfig;
            this.widgetConfig = widgetConfig;
            this.widgetData = widgetData;
        }

        public static WidgetDef createLabelCell(String id, String text) {
            WidgetDisplayConfig widgetDisplayConfig = new WidgetDisplayConfig();
            widgetDisplayConfig.setId(id);

            CellConfig cellConfig = new CellConfig();
            cellConfig.setHorizontalAlignment("right");
            cellConfig.setWidgetDisplayConfig(widgetDisplayConfig);

            LabelConfig labelConfig = new LabelConfig();
            labelConfig.setId(id);
            labelConfig.setText(text);

            LabelData data = new LabelData();
            data.setLabel(text);

            return new WidgetDef(cellConfig, labelConfig, data);
        }

        public static WidgetDef createTextBoxCell(String id, String text, String colspan) {
            WidgetDisplayConfig widgetDisplayConfig = new WidgetDisplayConfig();
            widgetDisplayConfig.setId(id);

            CellConfig cellConfig = new CellConfig();
            cellConfig.setHorizontalAlignment("left");
            cellConfig.setWidgetDisplayConfig(widgetDisplayConfig);
            cellConfig.setColumnSpan(colspan);

            TextBoxConfig widgetConfig = new TextBoxConfig();
            widgetConfig.setId(id);
            widgetConfig.setFieldPathConfig(null);

            TextBoxData data = new TextBoxData();
            data.setText(text);

            return new WidgetDef(cellConfig, widgetConfig, data);
        }

        public static WidgetDef createIntegerBoxCell(String id, Integer number, String colSpan, String rowSpan) {
            WidgetDisplayConfig widgetDisplayConfig = new WidgetDisplayConfig();
            widgetDisplayConfig.setId(id);

            CellConfig cellConfig = new CellConfig();
            cellConfig.setHorizontalAlignment("left");
            cellConfig.setWidgetDisplayConfig(widgetDisplayConfig);
            cellConfig.setColumnSpan(colSpan);
            cellConfig.setRowSpan(rowSpan);

            IntegerBoxConfig widgetConfig = new IntegerBoxConfig();
            widgetConfig.setId(id);
            widgetConfig.setFieldPathConfig(null);

            IntegerBoxData data = new IntegerBoxData();
            data.setValue(number);

            return new WidgetDef(cellConfig, widgetConfig, data);
        }
    }

    private <T extends ComponentHandler> T obtainHandler(String componentName) {
        boolean containsHandler = applicationContext.containsBean(componentName);
        return containsHandler ? (T) applicationContext.getBean(componentName) : null;
    }
}
