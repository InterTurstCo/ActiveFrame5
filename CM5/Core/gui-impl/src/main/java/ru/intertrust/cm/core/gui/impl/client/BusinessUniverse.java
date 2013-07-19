package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
public class BusinessUniverse implements EntryPoint {
    public void onModuleLoad() {
        Window.alert("Hello Model !" + new GenericDomainObject() + " !");
    }
}
