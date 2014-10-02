package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;
import ru.intertrust.cm.core.config.gui.form.widget.UniqueKeyValueConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;

import java.util.HashMap;
import java.util.List;

/**
 * Created by andrey on 23.09.14.
 */
@Service
@Scope("prototype")
public class FieldValueConfigToValueResolver {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private LiteralFieldValueParser literalFieldValueParser;

    @Autowired
    private PersonService personService;

    @Autowired
    private CrudService crudService;

    private FieldValueConfig fieldValueConfig;
    private String linkedObjectType;
    private DomainObject baseObject;
    private String fieldName;

    public FieldValueConfigToValueResolver() {
    }

    public FieldValueConfigToValueResolver(String fieldName, FieldValueConfig fieldValueConfig, String linkedObjectType, DomainObject baseObject) {
        this.fieldName = fieldName;
        this.fieldValueConfig = fieldValueConfig;
        this.linkedObjectType = linkedObjectType;
        this.baseObject = baseObject;
    }

    public Value resolve() {
        final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(linkedObjectType, fieldName);
        return resolve(fieldConfig);
    }

    public Value resolve(FieldConfig fieldConfig) {
        if (fieldValueConfig.isSetNull()) {
            return null;
        }
        if (fieldValueConfig.isSetBaseObject()) {
            if (baseObject != null) {
                return new ReferenceValue(baseObject.getId());
            }
        }
        if (fieldValueConfig.isSetCurrentUser()) {
            DomainObject currentPerson = personService.getCurrentPerson();
            return new ReferenceValue(currentPerson.getId());
        }
        if (fieldValueConfig.getUniqueKeyValueConfig() != null) {
            DomainObject domainObjectByUniqueKey = findDomainObjectByUniqueKey(fieldValueConfig.getUniqueKeyValueConfig());
            return new ReferenceValue(domainObjectByUniqueKey.getId());
        }
        return literalFieldValueParser.textToValue(fieldValueConfig, fieldConfig);
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
