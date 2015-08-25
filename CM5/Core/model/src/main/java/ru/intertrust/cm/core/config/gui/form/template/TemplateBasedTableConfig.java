package ru.intertrust.cm.core.config.gui.form.template;

import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.08.2015
 *         Time: 21:23
 */
@Root(name = TemplateBasedTableConfig.CONFIG_TAG_NAME)
public class TemplateBasedTableConfig extends TemplateBasedConfig {
    public static final String CONFIG_TAG_NAME = "template-based-table";

}
