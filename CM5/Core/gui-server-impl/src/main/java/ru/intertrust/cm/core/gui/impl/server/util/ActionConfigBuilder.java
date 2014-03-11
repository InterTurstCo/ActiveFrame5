package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.config.gui.BeforeExecutionConfig;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.ValidatorsConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.01.14
 *         Time: 13:15
 */
public class ActionConfigBuilder {
   public static ActionConfig createActionConfig(final String name, final String component,
                                                   final String label, final String imageUrl) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        return config;
   }

   public static ActionConfig createActionConfig(final String name, final String component,
                                                  final String label, final String imageUrl,
                                                  List<ValidatorConfig> validatorConfigs) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        config.setBeforeExecution(new BeforeExecutionConfig());
        config.getBeforeExecution().setValidatorsConfig(new ValidatorsConfig());
        config.getBeforeExecution().getValidatorsConfig().setValidators(new ArrayList(validatorConfigs));
        return config;
   }
}
