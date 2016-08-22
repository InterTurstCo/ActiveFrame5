package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.08.2016
 * Time: 9:44
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyHistoryManager implements HierarchyPluginConstants {

    public void saveHistory(HierarchyPluginData pData){
        Command command = new Command(SAVE_PLUGIN_HISTORY, PLUGIN_COMPONENT_NAME, pData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Something was going wrong while saving plugin history");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                GWT.log("Plugin history successfully saved.");
            }
        });
    }
}
