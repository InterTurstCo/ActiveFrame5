package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.11.2014
 *         Time: 17:45
 */
@Root(name = "param")
public class SelectionParamConfig extends ComplexParamConfig {

    @Override
    public String getWidgetId() {
        return null; // do not throwing exception to keep common code working
    }

}
