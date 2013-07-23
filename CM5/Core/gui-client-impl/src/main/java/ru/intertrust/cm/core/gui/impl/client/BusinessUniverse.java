package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentName;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse implements EntryPoint, Component {
    public void onModuleLoad() {
        //Window.alert("Hello Model !" + new GenericDomainObject() + " !");
        BusinessUniverse obj = ComponentRegistry.instance.get("business.universe");
        Window.alert(obj == null ? "null" : obj.toString());
    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }
}
