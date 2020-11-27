package ru.intertrust.cm.core.business.impl.search.simple;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldConfig;
import ru.intertrust.cm.core.config.SimpleDataFieldType;
import ru.intertrust.cm.core.model.FatalException;

@Service
public class DefaultSimpleSearchUtils implements SimpleSearchUtils {

    private final AtomicReference<Map<String, SimpleDataFieldConfig>> fieldConfigsReference = new AtomicReference<>(null);

    @Override
    public String getSolrFieldName(SimpleDataConfig config, String fieldName) {
        SimpleDataFieldConfig fieldConfig = getFieldConfig(config, fieldName);
        String result;
        if (fieldConfig.getType().equals(SimpleDataFieldType.String)) {
            result = "cm_r";
        } else if (fieldConfig.getType().equals(SimpleDataFieldType.Long)) {
            result = "cm_l";
        } else if (fieldConfig.getType().equals(SimpleDataFieldType.Bollean)) {
            result = "cm_b";
        } else if (fieldConfig.getType().equals(SimpleDataFieldType.DateTime)) {
            result = "cm_dt";
        } else if (fieldConfig.getType().equals(SimpleDataFieldType.Date)) {
            result = "cm_dt";
        } else {
            throw new FatalException("Field " + fieldName + ". Type " + fieldConfig.getType() + " is not supported");
        }

        if (fieldConfig.isMultivalue() != null && fieldConfig.isMultivalue()) {
            result += "s_";
        } else {
            result += "_";
        }

        result += fieldName;

        return result;
    }

    /**
     * Кэшируем тип полей
     *
     * @param config конфигурация
     * @param name наименование поля
     * @return конфиг для поля
     */
    @Override
    public SimpleDataFieldConfig getFieldConfig(SimpleDataConfig config, String name) {
        Map<String, SimpleDataFieldConfig> configMap = fieldConfigsReference.get();
        if (configMap == null) {
            initFieldConfigs(config);
        }
        Map<String, SimpleDataFieldConfig> configs = fieldConfigsReference.get();
        return configs.get(config.getName().toLowerCase() + ":" + name.toLowerCase());
    }

    private void initFieldConfigs(SimpleDataConfig config) {
        Map<String, SimpleDataFieldConfig> map = config.getFields().stream()
                .collect(Collectors.toMap(it -> config.getName().toLowerCase() + ":" + it.getName().toLowerCase(), it -> it));
        fieldConfigsReference.compareAndSet(null, map);
    }

}
