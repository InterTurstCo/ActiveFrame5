package ru.intertrust.cm.core.gui.impl.server.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.config.gui.action.AbstractActionEntryConfig;
import ru.intertrust.cm.core.config.gui.action.ActionEntryConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.gui.model.action.ActionEntryContext;
import static ru.intertrust.cm.core.gui.model.util.ComponentAttributeHelper.*;


/**
 * @author Sergey.Okolot
 *         Created on 16.04.2014 17:53.
 */
public class ActionConfig2ContextConverter {

    /**
     * Don't create instance of helper class.
     */
    private ActionConfig2ContextConverter() {}

    public static Map<String, Serializable> getAttributes(final AbstractActionEntryConfig config) {
        return null;
    }

    private interface Extractor<T> {

        Map<String, Serializable> getAttributes(T config);
    }

    private static class ActionRefExtractor implements Extractor<ActionRefConfig> {
        private ActionRefExtractor() {
        }

        @Override
        public Map<String, Serializable> getAttributes(ActionRefConfig config) {
            final Map<String, Serializable> result = new HashMap<>();

            return null;
        }
    }

//    private static class ActionEntryExtractor implements Extractor<ActionEntryConfig> {
//        @Override
//        public Map<String, Serializable> getAttributes(ActionEntryConfig config) {
//            final Map<String, Serializable> ctx = new HashMap<>();
//            ctx.setAttribute(ID_ATTR, config.getId());
//            ctx.setAttribute(STYLE_ATTR, config.getStyle());
//            ctx.setAttribute(STYLE_CLASS_ATTR, config.getStyleClass());
//            ctx.setAttribute(ADD_STYLE_CLASS_ATTR, config.getAddStyleClass());
//            ctx.setAttribute(ORDER_ATTR, config.getOrder());
//            ctx.setAttribute(DISABLED_ATTR, config.isDisabled());
//            ctx.setAttribute(MERGED_ATTR, config.isMerged());
//            ctx.setAttribute(COMPONENT_NAME_ATTR, config.getComponentName());
//            ctx.setAttribute(TEXT_ATTR, config.getComponentName());
//            ctx.setAttribute(IMAGE_ATTR, config.getImage());
//            ctx.setAttribute(TOOLTIP_ATTR, config.getTooltip());
//            ctx.setAttribute(IMMEDIATE_ATTR, config.isImmediate());
//            ctx.setAttribute(ACTION_ATTR, config.getAction());
//            ctx.setAttribute(DISPLAY_ATTR, config.getDisplay());
//            ctx.setAttribute(GROUP_ID_ATTR, config.getGroupId());
//            ctx.setAttribute(DIRTY_SENSITIVITY_ATTR, config.isDirtySensitivity());
//            final ArrayList<ActionEntryContext> children = new ArrayList<>(config.getChildren().size());
//            for (ActionEntryConfig childConfig : config.getChildren()) {
//                final ActionEntryContext childCtx = new ActionEntryContext();
//                fillActionEntryContext(childCtx, childConfig);
//                children.add(childCtx);
//            }
//            if (!children.isEmpty()) {
//                ctx.setAttribute(CHILDREN_ATTR, children);
//            }
//        }
//    }
}
