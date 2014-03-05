package ru.intertrust.cm.core.tools;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddressee;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeContextRole;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeDynamicGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseeGroup;
import ru.intertrust.cm.core.business.api.dto.notification.NotificationAddresseePerson;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Класс конвертера мапинга xml на java класс и оьбратно информации об адресатах в процессах Activiti
 * @author larin
 * 
 */
public class AddresseeConverter implements Converter<NotificationAddressee> {

    @Override
    public NotificationAddressee read(InputNode node) throws Exception {
        NotificationAddressee result = null;
        if (node.getName().equals("person-addressee")) {
            result =
                    new NotificationAddresseePerson(getIdService().createId(node.getAttribute("person-id").getValue()));
        } else if (node.getName().equals("group-addressee")) {
            result = new NotificationAddresseeGroup(getIdService().createId(node.getAttribute("group-id").getValue()));
        } else if (node.getName().equals("dynamic-group-addressee")) {
            result =
                    new NotificationAddresseeDynamicGroup(node.getAttribute("group-name").getValue(), getIdService()
                            .createId(node.getAttribute("context-id").getValue()));
        } else if (node.getName().equals("context-role-addressee")) {
            result =
                    new NotificationAddresseeContextRole(node.getAttribute("role-name").getValue(), getIdService()
                            .createId(node.getAttribute("context-id").getValue()));
        }
        return result;
    }

    @Override
    public void write(OutputNode node, NotificationAddressee value) throws Exception {
        if (value instanceof NotificationAddresseePerson) {
            node.setName("person-addressee");
            node.setAttribute("person-id", ((NotificationAddresseePerson) value).getPersonId().toStringRepresentation());
        } else if (value instanceof NotificationAddresseeGroup) {
            node.setName("group-addressee");
            node.setAttribute("group-id", ((NotificationAddresseeGroup) value).getGroupId().toStringRepresentation());
        } else if (value instanceof NotificationAddresseeDynamicGroup) {
            node.setName("dynamic-group-addressee");
            node.setAttribute("group-name", ((NotificationAddresseeDynamicGroup) value).getGroupName());
            node.setAttribute("context-id", ((NotificationAddresseeDynamicGroup) value).getContextId()
                    .toStringRepresentation());
        } else if (value instanceof NotificationAddresseeContextRole) {
            node.setName("context-role-addressee");
            node.setAttribute("role-name", ((NotificationAddresseeContextRole) value).getRoleName());
            node.setAttribute("context-id", ((NotificationAddresseeContextRole) value).getContextId()
                    .toStringRepresentation());
        }
    }

    private IdService getIdService() {
        return SpringApplicationContext.getContext().getBean(IdService.class);
    }

}
