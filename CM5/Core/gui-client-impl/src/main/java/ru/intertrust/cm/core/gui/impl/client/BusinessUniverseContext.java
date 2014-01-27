package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.gui.api.client.ApplicationContext;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 14:32
 *
 */
public class BusinessUniverseContext extends ApplicationContext {

    // данные о конфигурации областей поиска и доменных объектах
    private HashMap<String, ArrayList<String>> searchAreasData = new HashMap<String, ArrayList<String>>();
    private ExtendedSearchPluginData extendedSearchData = new ExtendedSearchPluginData();

    public HashMap<String, ArrayList<String>> getSearchAreasData() {
        return searchAreasData;
    }

    public void setSearchAreasData(HashMap<String, ArrayList<String>> searchAreasData) {
        this.searchAreasData = searchAreasData;
    }

    public void getSearchConfiguration() {
        AsyncCallback<ExtendedSearchPluginData> callback = new AsyncCallback<ExtendedSearchPluginData>() {
            @Override
            public void onSuccess(ExtendedSearchPluginData result) {
                setSearchAreasData(result.getSearchAreasData());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("BusinessUniverseContext не получил конфигурацию для поиска !!!");
            }
        };
        Command command = new Command("searchConfigurations", "extended.search.plugin", extendedSearchData);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
    }
}
