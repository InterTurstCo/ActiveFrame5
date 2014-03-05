package ru.intertrust.cm.core.tools;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;

@Root(name = "notification-addressee")
public class NotificationAddresseeConverter implements Dto {
    private static final long serialVersionUID = -2792065572057873669L;

    @ElementList(name="addressee-list")
    private List<NotificationAddressee> addressee = new ArrayList<NotificationAddressee>();

    public NotificationAddresseeConverter() {
    }

    public List<NotificationAddressee> getAddresseeList(){
        return addressee;
    }
    
    public static NotificationAddresseeConverter load(String xmlString) throws Exception {
        Serializer serializer = getSerializer();
        return serializer.read(NotificationAddresseeConverter.class, xmlString);
    }

    public NotificationAddresseeConverter addPerson(Id personId) {
        addressee.add(new NotificationAddresseePerson(personId));
        return this;
    }

    public NotificationAddresseeConverter addGroup(Id groupId) {
        addressee.add(new NotificationAddresseeGroup(groupId));
        return this;
    }

    public NotificationAddresseeConverter addDynamicGroup(String groupName, Id contextId) {
        addressee.add(new NotificationAddresseeDynamicGroup(groupName, contextId));
        return this;
    }

    public NotificationAddresseeConverter addContextRole(String roleName, Id contextId) {
        addressee.add(new NotificationAddresseeContextRole(roleName, contextId));
        return this;
    }

    @Override
    public String toString() {
        try {
            Serializer serializer = getSerializer();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            serializer.write(this, out);
            return out.toString("UTF8");
        } catch (Exception ex) {
            throw new RuntimeException("Error serialise Notification Addressee", ex);
        }
    }
    
    private static Serializer getSerializer() throws Exception{
        Registry registry = new Registry();
        registry.bind(NotificationAddresseePerson.class, AddresseeConverter.class);
        registry.bind(NotificationAddresseeGroup.class, AddresseeConverter.class);
        registry.bind(NotificationAddresseeDynamicGroup.class, AddresseeConverter.class);
        registry.bind(NotificationAddresseeContextRole.class, AddresseeConverter.class);
        Strategy strategy = new RegistryStrategy(registry);
        Serializer serializer = new Persister(strategy);
        return serializer;
    }
}
