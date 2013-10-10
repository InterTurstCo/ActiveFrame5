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
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

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
        ComponentHandler componentHandler = obtainHandler(command.getComponentName());
        if (componentHandler == null) {
            log.warn("handler for component '{}' not found", command.getComponentName());
            return null;
        }
        try {
            return (Dto) componentHandler.getClass().getMethod(command.getName(), Dto.class)
                    .invoke(componentHandler, command.getParameter());
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
        FormObjects formObjects = form.getObjects();

        HashSet<FieldPath> objectsFieldPathsToSave = new HashSet<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetData widgetData = form.getWidgetData(widgetConfig.getId());
            if (widgetData == null) { // ignore - such data shouldn't be saved
                continue;
            }
            Value newValue = widgetData.toValue();
            FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
            Value oldValue = formObjects.getObjectValue(fieldPath);
            if (!newValue.equals(oldValue)) {
                formObjects.setObjectValue(fieldPath, newValue);
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
            toSave.add(formObjects.getObject(fieldPath));
        }
        crudService.save(toSave);
        DomainObject rootDomainObject = formObjects.getObject(FieldPath.ROOT);
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
        FormObjects formObjects = getFormObjects(root, widgetConfigs);
        for (WidgetConfig config : widgetConfigs) {
            WidgetHandler componentHandler = obtainHandler(config.getComponentName());
            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            widgetDataMap.put(config.getId(), componentHandler.getInitialDisplayData(widgetContext));
        }
        Form form = new Form(formConfig.getName(), formConfig.getMarkup(), widgetDataMap, formObjects, formConfig.getDebug());
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

    private List<FieldPath> getFieldPaths(List<WidgetConfig> configs) {
        List<FieldPath> paths = new ArrayList<>(configs.size());
        for (WidgetConfig config : configs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + config.getId() + " is not configured with Field Path");
                } else {
                    continue;
                }
            }
            paths.add(new FieldPath(fieldPathConfig.getValue()));
        }
        return paths;
    }

    private FormObjects getFormObjects(DomainObject root, List<WidgetConfig> widgetConfigs) {
        // не уверен, нужен ли здесь будет Business Object, но наверно нужен в некотором урезанном виде - для оптимистических блокировок

        List<FieldPath> fieldPaths = getFieldPaths(widgetConfigs);

        FormObjects formObjects = new FormObjects();
        formObjects.setRootObject(root);
        for (FieldPath fieldPath : fieldPaths) {
            DomainObject currentRoot = root;
            for (Iterator<FieldPath> subPathIterator = fieldPath.subPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                if (!subPathIterator.hasNext()) { // значит текущий путь указывает на Value и будет получаться из Domain Object
                    break; // ничего не делаем, а раз следующего нет, выходим из цикла
                }
                if (formObjects.isObjectSet(subPath)) {
                    continue;
                }

                String linkField = subPath.getLastElement();
                if (linkField.contains("^")) { // it's a "back-link"
                    //todo
                } else {
                    Id linkedObjectId = currentRoot.getReference(linkField);
                    if (linkedObjectId != null) {
                        DomainObject linkedDo = crudService.find(linkedObjectId);
                        formObjects.setObject(subPath, linkedDo);
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

        return formObjects;
    }

    private <T extends ComponentHandler> T obtainHandler(String componentName) {
        boolean containsHandler = applicationContext.containsBean(componentName);
        return containsHandler ? (T) applicationContext.getBean(componentName) : null;
    }
}
