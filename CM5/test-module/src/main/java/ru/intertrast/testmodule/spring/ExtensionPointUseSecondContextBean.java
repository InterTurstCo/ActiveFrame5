package ru.intertrast.testmodule.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint(context = "second-context-name", filter = "department_test")
public class ExtensionPointUseSecondContextBean implements AfterSaveExtensionHandler {

    @Autowired
    private TestSpringBeanInSecondContext testBean;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        testBean.testMethod();
    }

}
