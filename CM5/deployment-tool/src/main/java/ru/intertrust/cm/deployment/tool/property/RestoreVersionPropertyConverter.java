package ru.intertrust.cm.deployment.tool.property;

import org.springframework.core.convert.converter.Converter;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
public class RestoreVersionPropertyConverter implements Converter<String, RestoreVersionType> {

    @Override
    public RestoreVersionType convert(String s) {
        return RestoreVersionType.getType(s);
    }
}
