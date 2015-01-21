package ru.intertrust.cm.core.gui.impl.server.form.defaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.DefaultValueConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ComboBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DecimalBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.IntegerBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectHyperlinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ListBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RadioButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TextAreaConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TextBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.DateBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormDefaultValueSetter;
import ru.intertrust.cm.core.gui.impl.server.form.FieldValueConfigToValueResolver;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrey on 17.09.14.
 */
public class FormDefaultValueSetterImpl implements FormDefaultValueSetter {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ConfigurationExplorer configurationExplorer;

    private Map<String, DefaultValueConfig> defaultValueConfigCache = new HashMap<>();
    private Map<String, List<DefaultValueConfig>> defaultValueConfigsCache = new HashMap<>();
    private FormConfig formConfig;
    private FormMappingConfig mappingConfig;

    public FormDefaultValueSetterImpl() {
    }

    public FormDefaultValueSetterImpl(FormConfig formConfig, FormMappingConfig mappingConfig) {
        this.formConfig = formConfig;
        this.mappingConfig = mappingConfig;
    }

    @PostConstruct
    private void initCaches() {
        initCachesFromWidgetConfigs(formConfig);
        if (mappingConfig != null) {
            initCaches(mappingConfig);
        } else {
            initCachesFromGlobalConfig(formConfig);
        }
    }

    @Override
    public Value[] getDefaultValues(FormObjects formObjects, FieldPath fieldPath) {
        List<Value> values = new ArrayList<>();
        List<DefaultValueConfig> defaultValueConfigList = defaultValueConfigsCache.get(fieldPath.getPath());
        if (defaultValueConfigList != null) {
            for (DefaultValueConfig defaultValueConfig : defaultValueConfigList) {
                values.add(resolveValue(formObjects, defaultValueConfig, fieldPath));
            }
        }
        return values.toArray(new Value[values.size()]);
    }

    @Override
    public Value getDefaultValue(FormObjects formObjects, FieldPath fieldPath) {
        DefaultValueConfig defaultValueConfig = defaultValueConfigCache.get(fieldPath.getPath());
        if (defaultValueConfig == null) {
            return null;
        }
        return resolveValue(formObjects, defaultValueConfig, fieldPath);

    }

    private Value resolveValue(FormObjects formObjects, DefaultValueConfig defaultValueConfig, FieldPath fieldPath) {
        FieldValueConfig fieldValueConfig = defaultValueConfig.getFieldValueConfig();
        FieldValueConfigToValueResolver resolver = (FieldValueConfigToValueResolver)
                applicationContext.getBean("fieldValueConfigToValueResolver", fieldPath.getFieldName(), fieldValueConfig, formObjects.getRootDomainObjectType(), null);
        if (formConfig.getType().equals(FormConfig.TYPE_REPORT)) {
            for (WidgetConfig widgetConfig : formConfig.getWidgetConfigurationConfig().getWidgetConfigList()) {
                String value = widgetConfig.getFieldPathConfig().getValue();
                if (value != null && value.equals(fieldPath.getPath())) {
                    FieldConfig fieldConfig = createFieldConfigBasedOnWidgetType(widgetConfig);
                    return resolver.resolve(fieldConfig);
                }
            }
        }
        return resolver.resolve();
    }

    private FieldConfig createFieldConfigBasedOnWidgetType(WidgetConfig widgetConfig) {
        FieldConfig fieldConfig = null;
        //TODO only type of widget can make us aware of field type in reports :((
        if (widgetConfig instanceof LabelConfig
                || widgetConfig instanceof TextBoxConfig
                || widgetConfig instanceof TextAreaConfig) {
            fieldConfig = new FieldConfig() {
                @Override
                public FieldType getFieldType() {
                    return FieldType.STRING;
                }
            };
        } else if (widgetConfig instanceof IntegerBoxConfig) {
            fieldConfig = new FieldConfig() {
                @Override
                public FieldType getFieldType() {
                    return FieldType.LONG;
                }
            };
        } else if (widgetConfig instanceof DecimalBoxConfig) {
            fieldConfig = new FieldConfig() {
                @Override
                public FieldType getFieldType() {
                    return FieldType.DECIMAL;
                }
            };
        } else if (widgetConfig instanceof SuggestBoxConfig || widgetConfig instanceof HierarchyBrowserConfig ||
                widgetConfig instanceof LinkedDomainObjectHyperlinkConfig ||
                widgetConfig instanceof LinkedDomainObjectsTableConfig ||
                widgetConfig instanceof ListBoxConfig || widgetConfig instanceof ComboBoxConfig || widgetConfig instanceof RadioButtonConfig) {
            fieldConfig = new FieldConfig() {
                @Override
                public FieldType getFieldType() {
                    return FieldType.REFERENCE;
                }
            };
        } else if (widgetConfig instanceof DateBoxConfig) {
            fieldConfig = new FieldConfig() {
                @Override
                public FieldType getFieldType() {
                    return FieldType.DATETIME;
                }
            };
        }
        return fieldConfig;
    }

    private void initCachesFromGlobalConfig(FormConfig formConfig) {
        Collection<FormMappingsConfig> configs = configurationExplorer.getConfigs(FormMappingsConfig.class);
        for (FormMappingsConfig config : configs) {
            for (FormMappingConfig formMappingConfig : config.getFormMappingConfigList()) {
                if (formConfig.getType() != null && formConfig.getType().equals(FormConfig.TYPE_REPORT)) {
                    if (formMappingConfig.getForm().equals(formConfig.getName())) {
                        for (FieldPathConfig formMappingFieldPathConfig : formMappingConfig.getFieldPathConfigs()) {
                            putToCaches(formMappingFieldPathConfig);
                        }
                    }

                } else {
                    String domainObjectType = formMappingConfig.getDomainObjectType();
                    String form = formMappingConfig.getForm();
                    if (domainObjectType != null && form != null) {
                        if (domainObjectType.equals(formConfig.getDomainObjectType()) && form.equals(formConfig.getName())) {
                            for (FieldPathConfig formMappingFieldPathConfig : formMappingConfig.getFieldPathConfigs()) {
                                putToCaches(formMappingFieldPathConfig);
                            }
                        }
                    }
                }
            }
        }
    }

    private void initCaches(FormMappingConfig mappingConfig) {
        for (FieldPathConfig fieldPathConfig : mappingConfig.getFieldPathConfigs()) {
            putToCaches(fieldPathConfig);
        }
    }

    private void initCachesFromWidgetConfigs(FormConfig formConfig) {
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            if (widgetConfig.getFieldPathConfig() != null) {
                putToCaches(widgetConfig.getFieldPathConfig());
            }
        }
    }

    private void putToCaches(FieldPathConfig fieldPathConfig) {
        if (fieldPathConfig.getDefaultValueConfig() != null) {
            defaultValueConfigCache.put(fieldPathConfig.getValue(), fieldPathConfig.getDefaultValueConfig());
        } else if (fieldPathConfig.getDefaultValuesConfig() != null) {
            defaultValueConfigsCache.put(fieldPathConfig.getValue(), fieldPathConfig.getDefaultValuesConfig().getDefaultValueConfigs());
        }
    }
}
