package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.config.ConfigurationExplorer;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.2014
 *         Time: 21:08
 */
public abstract class FormProcessor {
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ConfigurationExplorer configurationExplorer;
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected PersonService personService;
    @Autowired
    protected AttachmentService attachmentService;

    private String userUid;

    public String getUserUid() {
        if (userUid == null) {
            userUid = personService.getCurrentPersonUid();
        }
        return userUid;
    }
}
