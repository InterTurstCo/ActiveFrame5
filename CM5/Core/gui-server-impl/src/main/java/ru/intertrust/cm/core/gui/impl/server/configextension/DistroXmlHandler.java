package ru.intertrust.cm.core.gui.impl.server.configextension;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.TextAreaHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;

import java.util.List;

/**
 * Created by Ravil on 15.05.2017.
 */
@ComponentName("distro.xml.handler")
public class DistroXmlHandler extends TextAreaHandler {

    private static final String FIELD_NAME = "name";

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private ConfigurationSerializer configurationSerializer;

    private String tagName;

    @Override
    public TextState getInitialState(WidgetContext context) {
        TextState tState = super.getInitialState(context);
        if(context.getFormObjects().getRootDomainObject()!=null){
            tagName = context.getFormObjects().getRootDomainObject().getString(FIELD_NAME);
            List<TopLevelConfig> configs =  configurationExplorer.getDistributiveConfiguration().getConfigurationList();
            for(TopLevelConfig c : configs){
                if(c.getName().equals(tagName)){
                    tState.setText(configurationSerializer.serializeConfiguration(c));
                    break;
                }
            }
        }
        return tState;
    }
}
