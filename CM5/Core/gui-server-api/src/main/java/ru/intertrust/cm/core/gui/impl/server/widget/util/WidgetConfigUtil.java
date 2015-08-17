package ru.intertrust.cm.core.gui.impl.server.widget.util;

import org.apache.commons.collections.Predicate;
import ru.intertrust.cm.core.config.gui.form.DefaultValueSetterConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.WidgetStatesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 28.02.2015
 *         Time: 17:18
 */
public class WidgetConfigUtil {
    public static Collection<String> getRequiredWidgetIdsFromForm(FormConfig formConfig, Collection<WidgetConfig> parentWidgetConfigs){
        if(formConfig == null || formConfig.getDefaultValueSetterConfig() == null
                || formConfig.getDefaultValueSetterConfig().getWidgetStatesConfig() == null){
            return Collections.EMPTY_LIST;
        }
        DefaultValueSetterConfig defaultValueSetterConfig = formConfig.getDefaultValueSetterConfig();
        WidgetStatesConfig widgetStatesConfig = defaultValueSetterConfig.getWidgetStatesConfig();
        Predicate predicate = getPredicate(widgetStatesConfig);
        return getRequiredWidgetIds(parentWidgetConfigs, predicate);
    }
    private static Collection<String> getRequiredWidgetIds(Collection<WidgetConfig> widgetConfigs, Predicate predicate){
        List<String> requireWidgetIds = new ArrayList<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            if(predicate.evaluate(widgetConfig)){
                requireWidgetIds.add(widgetConfig.getId());
            }
        }
        return requireWidgetIds;
    }

    private static Predicate getPredicate(WidgetStatesConfig widgetStatesConfig){
        Predicate predicate = null;
        if(widgetStatesConfig.getEditableWidgetsIndicationConfig() != null){
            predicate = new EditableWidgetsPredicate();
        }else if(widgetStatesConfig.getWidgetsIndicationConfig() != null){
            predicate = new WidgetsIdsPredicate(widgetStatesConfig.getWidgetsIndicationConfig().getIds());
        } else {
            predicate = new AllWidgetsPredicate();
        }
        return predicate;
    }
    private static class AllWidgetsPredicate implements Predicate {

        @Override
        public boolean evaluate(Object input) {
            return true;
        }
    }

    private static class EditableWidgetsPredicate implements Predicate {

        @Override
        public boolean evaluate(Object input) {
            WidgetConfig config = (WidgetConfig) input;
            return !config.getReadOnly();
        }
    }

    private static class WidgetsIdsPredicate implements Predicate {
        private Collection<String> widgetIds;

        public WidgetsIdsPredicate(Collection<String> widgetIds) {
            this.widgetIds = widgetIds;
        }

        @Override
        public boolean evaluate(Object input) {
            WidgetConfig config = (WidgetConfig) input;
            return widgetIds.contains(config.getId());
        }
    }
}
