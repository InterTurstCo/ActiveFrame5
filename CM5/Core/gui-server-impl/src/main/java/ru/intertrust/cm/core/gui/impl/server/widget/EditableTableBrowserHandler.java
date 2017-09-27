package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserConfig;
import ru.intertrust.cm.core.gui.api.server.widget.DefaultValueProvider;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.EditableBrowserRequestData;
import ru.intertrust.cm.core.gui.model.form.widget.EditableTableBrowserState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * Created by Ravil on 26.09.2017.
 */
@ComponentName("editable-table-browser")
public class EditableTableBrowserHandler  extends ValueEditingWidgetHandler {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public EditableTableBrowserState getInitialState(WidgetContext context) {
        EditableTableBrowserState state = new EditableTableBrowserState();
        final FieldConfig fieldConfig = getFieldConfig(context);
        state.setText(context.<String>getFieldPlainValue());
        state.setEditableTableBrowserConfig((EditableTableBrowserConfig) context.getWidgetConfig());
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        return new StringValue(((EditableTableBrowserState) state).getText());
    }

    public Dto getDefaultValue(Dto request){
        EditableBrowserRequestData rEquest = (EditableBrowserRequestData)request;
        if(rEquest.getConfig().getDefaultComponentName()!=null &&
                applicationContext.containsBean(rEquest.getConfig().getDefaultComponentName())){
            DefaultValueProvider provider = (DefaultValueProvider)applicationContext.getBean(rEquest.getConfig().getDefaultComponentName());
            ((EditableBrowserRequestData) request).setDefaultValue(provider.provide(((EditableBrowserRequestData) request).getFormState()));

            return rEquest;
        } else throw new GuiException("Не найден компонент предоставляющий значение по умолчанию");
    }
}
