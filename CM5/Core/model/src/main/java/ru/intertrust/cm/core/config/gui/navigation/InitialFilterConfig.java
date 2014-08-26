package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "initial-filter")
public class InitialFilterConfig extends AbstractFilterConfig implements Dto {
    public void addParamConfig(ParamConfig paramConfig){
        List<ParamConfig> paramConfigs = getParamConfigs();
        if(paramConfigs == null) {
            paramConfigs = new ArrayList<>(1);
        }
        paramConfigs.add(paramConfig);
    }

}
