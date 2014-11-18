package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 27.06.2014
 *         Time: 20:18
 */
public class DomainObjectFieldsSetter {
    @Autowired
    protected ApplicationContext applicationContext;

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
        FieldValueConfigToValueResolver resolver = (FieldValueConfigToValueResolver) applicationContext.getBean("fieldValueConfigToValueResolver", fieldName, fieldValueConfig, linkedObject.getTypeName(), baseObject);
        Value value = resolver.resolve();
        if (value instanceof ReferenceValue) {
            ReferenceValue referenceValue = (ReferenceValue) value;
            linkedObject.setReference(fieldName, referenceValue.get());
        } else {
            linkedObject.setValue(fieldName, value);
        }
    }

}
