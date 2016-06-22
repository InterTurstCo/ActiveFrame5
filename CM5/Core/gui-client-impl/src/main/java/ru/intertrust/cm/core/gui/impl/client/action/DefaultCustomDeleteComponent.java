package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.CustomDelete;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEvent;
import ru.intertrust.cm.core.gui.api.client.event.CustomDeleteEventHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CustomDeleteData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 12:03
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("default.custom.delete.component")
public class DefaultCustomDeleteComponent extends BaseComponent implements CustomDelete {
    EventBus localEventBus;


    @Override
    public void delete(final Id id, EventBus eventBus) {
        localEventBus = eventBus;
        CustomDeleteData data = new CustomDeleteData();
        data.setObjectToDelete(id);

        Command command = new Command("delete", "default.custom.delete.component", data);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining details for stage ");
                localEventBus.fireEvent(new CustomDeleteEvent(id, CustomDeleteEventHandler.DeleteStatus.ERROR, caught.getMessage()));
            }

            @Override
            public void onSuccess(Dto result) {
                CustomDeleteData cData = (CustomDeleteData) result;
                if (cData.getResult() == null) {
                    localEventBus.fireEvent(new CustomDeleteEvent(id,CustomDeleteEventHandler.DeleteStatus.OK, "Успешно удалено..."));
                } else {
                    localEventBus.fireEvent(new CustomDeleteEvent(id, CustomDeleteEventHandler.DeleteStatus.ERROR, cData.getResult()));
                }
            }
        });

    }

    @Override
    public Component createNew() {
        return new DefaultCustomDeleteComponent();
    }
}
