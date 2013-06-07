package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * Helper для работы с конфигурацией бизнес-объектов
 * @author vmatsukevich
 *         Date: 5/18/13
 *         Time: 4:03 PM
 */
public class ConfigurationHelper {

    /**
     * Находит конфигурацию бизнес-объекта по имени
     * @param configuration конфигурация бизнес-объектов
     * @param name имя бизнес-объекта, конфигурацию которого надо найти
     * @return конфигурация бизнес-объекта
     */
    public static BusinessObjectConfig findBusinessObjectConfigByName(Configuration configuration, String name) {
        for(BusinessObjectConfig businessObjectConfig : configuration.getBusinessObjectConfigs()) {
            if(businessObjectConfig.getName().equals(name)) {
                return businessObjectConfig;
            }
        }
        throw new RuntimeException("BusinessObjectConfiguration is not found for name '" + name + "'");
    }
    
    /**
     * Находит конфигурацию бизнес-объекта по идентификатору
     * @param configuration конфигурация бизнес-объектов
     * @param id идентификатор бизнес-объекта, конфигурацию которого надо найти
     * @return конфигурация бизнес-объекта
     */
    public static BusinessObjectConfig findBusinessObjectConfigById(Configuration configuration, Id id) {
        for(BusinessObjectConfig businessObjectConfig : configuration.getBusinessObjectConfigs()) {
            if(businessObjectConfig.getId().equals(id)) {
                return businessObjectConfig;
            }
        }
        throw new RuntimeException("BusinessObjectConfiguration is not found with id '" + id + "'");
    }    

    /**
     * Находит конфигурацию поля бизнес-объекта
     * @param businessObjectConfig конфигурация бизнес-объекта
     * @param fieldName имя поля поля, конфигурацию которого надо найти
     * @return конфигурация поля бизнес-объекта
     */
    public static FieldConfig findFieldConfigForBusinessObject(BusinessObjectConfig businessObjectConfig, String fieldName) {
        for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return fieldConfig;
            }
        }
        throw new RuntimeException("FieldConfig with name " + fieldName + " is not found in business object '" + businessObjectConfig.getName() + "'");
    }
}
