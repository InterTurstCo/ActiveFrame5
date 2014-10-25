package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.server.widget.FieldExtractor;
import ru.intertrust.cm.core.gui.impl.server.widget.PatternIterator;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.2014
 *         Time: 6:25
 */
@ComponentName("field-extractor")
public class FieldExtractorImpl implements FieldExtractor {
    @Autowired
    private CrudService crudService;

    @Override
    public Value extractField(DomainObject domainObject, String fieldPath) {
        PatternIterator iterator = new PatternIterator(fieldPath);
        DomainObject tempObject = domainObject;
        while (iterator.moveToNext()) {
            PatternIterator.ReferenceType type = iterator.getType();
            switch (type) {
                case FIELD:
                    return domainObject.getValue(iterator.getValue());
                case DIRECT_REFERENCE:
                    Id referenceId = tempObject.getReference(iterator.getValue());
                    if (referenceId != null) {
                        tempObject = crudService.find(referenceId);
                    }
                    break;
                case BACK_REFERENCE_ONE_TO_ONE:
                    return null;

            }
        }
        return null;
    }
}
