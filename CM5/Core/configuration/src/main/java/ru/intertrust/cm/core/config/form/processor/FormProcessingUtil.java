package ru.intertrust.cm.core.config.form.processor;

import org.springframework.beans.BeanUtils;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.08.2015
 *         Time: 23:39
 */
public class FormProcessingUtil {

    public static void copyNotNullProperties(Object source, Object target){
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    public static void failIfErrors(String formName, List<String> errors) {
        if (!errors.isEmpty()) {
            StringBuilder builder = new StringBuilder("Configuration of form with name '");
            builder.append(formName);
            builder.append("' was built with errors. Count: ");
            builder.append(errors.size());
            builder.append(" Content:\n");
            for (String error : errors) {
                builder.append(error);

            }
            throw new ConfigurationException(builder.toString());

        }
    }

    private static String[] getNullPropertyNames (Object source) {
        Set<String> emptyNames = new HashSet<>();
        List<Field> fields = new ArrayList<>();
        ReflectionUtil.fillAllFields(fields, source.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(source);
                if(fieldValue == null){
                    emptyNames.add(field.getName());
                }
            } catch (IllegalAccessException e) {
              //nothing could be done
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);

    }


}
