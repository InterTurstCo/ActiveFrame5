package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.form.FormAfterSaveInterceptor;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.2014
 *         Time: 15:18
 */
@ComponentName("a1.after.save.handler")
public class A1FormAfterSaveInterceptor implements FormAfterSaveInterceptor {
    @Autowired
    private CrudService crudService;

    @Override
    public DomainObject afterSave(DomainObject rootDomainObject) {
        rootDomainObject.setString("name2", "A1_NAME2 - FOREVER");
        return crudService.save(rootDomainObject);
    }
}
