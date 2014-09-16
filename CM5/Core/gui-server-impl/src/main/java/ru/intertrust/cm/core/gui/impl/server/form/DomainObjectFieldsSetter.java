package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.config.gui.form.widget.UniqueKeyValueConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;

import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 27.06.2014
 *         Time: 20:18
 */
public class DomainObjectFieldsSetter {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private LiteralFieldValueParser literalFieldValueParser;

    @Autowired
    private PersonService personService;

    @Autowired
    private CrudService crudService;

    private DomainObject linkedObject;
    private List<FieldValueConfig> fieldValueConfigs;
    private DomainObject baseObject;


    public DomainObjectFieldsSetter(final DomainObject domainObject, final List<FieldValueConfig> fieldValueConfigs, final DomainObject baseObject) {
        this.linkedObject = domainObject;
        this.fieldValueConfigs = fieldValueConfigs;
        this.baseObject = baseObject;
    }

    public void setFields() {
        for (FieldValueConfig fieldValueConfig : fieldValueConfigs) {
            setFieldValue(fieldValueConfig);
        }
    }

    private void setFieldValue(final FieldValueConfig fieldValueConfig) {
        final String fieldName = fieldValueConfig.getName();
        final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(linkedObject.getTypeName(), fieldName);
        if (fieldValueConfig.isSetNull()) {
            linkedObject.setValue(fieldName, null);
            return;
        }
        if (fieldValueConfig.isSetBaseObject()) {
            if (baseObject != null) {
                linkedObject.setReference(fieldName, baseObject);
            }
            return;
        }
        if (fieldValueConfig.isSetCurrentUser()) {
            linkedObject.setReference(fieldName, personService.getCurrentPerson());
            return;
        }
        if (fieldValueConfig.getUniqueKeyValueConfig() != null) {
            linkedObject.setReference(fieldName, findDomainObjectByUniqueKey(fieldValueConfig.getUniqueKeyValueConfig()));
        }

        final Value value = literalFieldValueParser.textToValue(fieldValueConfig, fieldConfig);
        linkedObject.setValue(fieldName, value);
    }

    private DomainObject findDomainObjectByUniqueKey(UniqueKeyValueConfig uniqueKeyValueConfig) {
        final List<FieldValueConfig> fieldValueConfigs = uniqueKeyValueConfig.getFieldValueConfigs();
        final String type = uniqueKeyValueConfig.getType();
        final HashMap<String, Value> uniqueKeyValuesByName = new HashMap<>(fieldValueConfigs.size());
        for (FieldValueConfig fieldValueConfig : fieldValueConfigs) {
            final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(type, fieldValueConfig.getName());
            uniqueKeyValuesByName.put(fieldValueConfig.getName(), literalFieldValueParser.textToValue(fieldValueConfig, fieldConfig));
        }

        return crudService.findByUniqueKey(type, uniqueKeyValuesByName);
    }

}
