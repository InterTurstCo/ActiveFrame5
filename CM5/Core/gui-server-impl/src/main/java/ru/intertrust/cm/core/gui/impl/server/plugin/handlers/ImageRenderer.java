package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;
import ru.intertrust.cm.core.config.gui.collection.view.MappingConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 13.02.14.
 */
public class ImageRenderer {
    private Map<Value, ImagePathValue> imagePathsMappings;

    public ImageRenderer() {

    }

    public void init(ImageMappingsConfig imageMappingsConfig, String fieldType) {
        imagePathsMappings = new HashMap<Value, ImagePathValue>();
        List<MappingConfig> mappings = imageMappingsConfig.getMappingConfigs();
        if (fieldType.equalsIgnoreCase("string")) {
            initStringValues(mappings);
        } else if (fieldType.equalsIgnoreCase("boolean")){
            initBooleanValues(mappings);
        }  else if (fieldType.equalsIgnoreCase("long")){
             initLongValues(mappings);
        }


    }

    private void initStringValues(List<MappingConfig> mappings) {
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("null")) {
                StringValue stringValueNull = new StringValue();
                imagePathsMappings.put(stringValueNull, imagePathValue);
                StringValue stringValueEmpty = new StringValue("");
                imagePathsMappings.put(stringValueEmpty, imagePathValue);
            } else {
                StringValue stringValue = new StringValue(value);
                imagePathsMappings.put(stringValue, imagePathValue);
            }
        }
    }
    private void initBooleanValues(List<MappingConfig> mappings) {
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("null")) {
                BooleanValue booleanValueNull = new BooleanValue();
                imagePathsMappings.put(booleanValueNull, imagePathValue);
            } else {
                BooleanValue booleanValue = new BooleanValue(Boolean.getBoolean(value));
                imagePathsMappings.put(booleanValue, imagePathValue);
            }
        }
    }
    private void initLongValues(List<MappingConfig> mappings) {
        for (MappingConfig config : mappings) {
            String value = config.getValue();
            ImagePathValue imagePathValue = new ImagePathValue(config.getImage());
            if (value.equalsIgnoreCase("null")) {
                LongValue longValueNull = new LongValue();
                imagePathsMappings.put(longValueNull, imagePathValue);
            } else {
                LongValue longValue = new LongValue(Long.getLong(value));
                imagePathsMappings.put(longValue, imagePathValue);
            }
        }
    }
    public ImagePathValue getImagePathValue(Value key) {
        return imagePathsMappings.get(key);
    }
}
