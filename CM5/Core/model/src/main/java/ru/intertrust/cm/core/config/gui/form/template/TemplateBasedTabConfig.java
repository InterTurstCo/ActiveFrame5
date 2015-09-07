package ru.intertrust.cm.core.config.gui.form.template;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.TabConfigMarker;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.08.2015
 *         Time: 21:32
 */
@Root(name = TemplateBasedTabConfig.CONFIG_TAG_NAME)
public class TemplateBasedTabConfig extends TemplateBasedConfig implements TabConfigMarker {
    public static final String CONFIG_TAG_NAME = "template-based-tab";
}
