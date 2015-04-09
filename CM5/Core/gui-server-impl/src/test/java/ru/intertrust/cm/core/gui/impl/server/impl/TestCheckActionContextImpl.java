package ru.intertrust.cm.core.gui.impl.server.impl;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.ActionContextChecker;

public class TestCheckActionContextImpl implements ActionContextChecker{

    @Override
    public boolean contextAvailable(DomainObject domainObject) {
        return "attr-4-value".equalsIgnoreCase(domainObject.getString("attr-4"));
    }
}
