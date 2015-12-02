package ru.intertrust.cm.core.config.form.processor;

import org.springframework.beans.BeanUtils;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
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

    private static void processWidgetIds(String prefix, List<WidgetConfig> widgetConfigs){
        for (WidgetConfig widgetConfig : widgetConfigs) {
            widgetConfig.setId(createId(prefix, widgetConfig.getId()));
        }
    }

    public static void processTabIds(String prefix, TabConfig tabConfig){
        tabConfig.setId(createId(prefix, tabConfig.getId()));
        List<TabGroupConfig> tabGroupConfigs = tabConfig.getGroupList().getTabGroupConfigs();
        for (TabGroupConfig tabGroupConfig : tabGroupConfigs) {
            processTabGroupIds(prefix, tabGroupConfig);
        }
    }

    private static void processTabGroupIds(String prefix, TabGroupConfig tabGroupConfig){
        tabGroupConfig.setId(createId(prefix, tabGroupConfig.getId()));
        processTableIds(prefix, tabGroupConfig.getLayout());
    }

    public static void processTableIds(String prefix, TableLayoutConfig tableLayoutConfig){
        List<RowConfig> rowConfigs = tableLayoutConfig.getRows();
        for (RowConfig rowConfig : rowConfigs) {
            processRowConfigIds(prefix, rowConfig);
        }
    }

    public static void processWidgetConfigs(String idsPrefix, List<WidgetConfig> formWidgetConfigs, List<WidgetConfig> template){
        List<WidgetConfig> cloned = ObjectCloner.getInstance().cloneObject(template);
        if(idsPrefix != null){
            FormProcessingUtil.processWidgetIds(idsPrefix, cloned);
        }
        formWidgetConfigs.addAll(cloned);
    }

    private static void processRowConfigIds(String prefix, RowConfig rowConfig){
        rowConfig.setId(createId(prefix, rowConfig.getId()));
        List<CellConfig> cellConfigs = rowConfig.getCells();
        for (CellConfig cellConfig : cellConfigs) {
            processCellConfigIds(prefix, cellConfig);
        }
    }

    private static void processCellConfigIds(String prefix, CellConfig cellConfig){
        cellConfig.setId(createId(prefix, cellConfig.getId()));
        WidgetDisplayConfig widgetDisplayConfig = cellConfig.getWidgetDisplayConfig();
        widgetDisplayConfig.setId(createId(prefix, widgetDisplayConfig.getId()));
    }

    private static String createId(String prefix, String previousId){
        return String.format("%s%s", prefix, previousId == null ? "" : previousId);
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
