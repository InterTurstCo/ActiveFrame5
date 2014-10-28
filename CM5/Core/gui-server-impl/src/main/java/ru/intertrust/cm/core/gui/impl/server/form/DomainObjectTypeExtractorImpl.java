package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.gui.api.server.widget.DomainObjectTypeExtractor;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.10.2014
 *         Time: 21:24
 */
@ComponentName("domain-object-type-extractor")
public class DomainObjectTypeExtractorImpl implements DomainObjectTypeExtractor {
    @Autowired
    private CrudService crudService;

    @Override
    public StringValue getType(Dto input) {
        Id id = (Id) input;
        return new StringValue(crudService.getDomainObjectType(id));
    }
}
