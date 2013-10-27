package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.user.client.rpc.RemoteService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 16:57
 */
public interface BusinessUniverseService extends RemoteService {
    BusinessUniverseInitialization getBusinessUniverseInitialization();

    Dto executeCommand(Command command)  throws GuiException;

    public FormDisplayData getForm(Id domainObjectId);
}
