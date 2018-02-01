package ru.intertrust.cm.core.gui.impl.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.SomePluginData;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync.*;

/**
 * Created by Ravil on 01.02.2018.
 */
@ComponentName("ServerService.plugin")
public class ServerService {
    private static Logger logger = Logger.getLogger("User Console Logger");

    private static void execute(String message) {
        Command command = new Command("writeLog", "ServerService.plugin",
                new SomePluginData(message));
        Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();
                logger.log(Level.SEVERE, caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {
                logger.log(Level.WARNING, "Сообщение залогировано на сервере");
            }
        });
    }

    public static void logInfo(String message){
        execute(message);
    }
}
