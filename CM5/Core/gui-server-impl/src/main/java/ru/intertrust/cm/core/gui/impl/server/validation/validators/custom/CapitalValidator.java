package ru.intertrust.cm.core.gui.impl.server.validation.validators.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.ServerValidator;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.ArrayList;

/**
 * Пример кастомного серверного валидатора. Проверяет, что столица не является городом другой страны.
 * @author Lesia Puhova
 *         Date: 10.03.14
 *         Time: 13:58
 */
public class CapitalValidator implements ServerValidator {

    private Id rootCountryId;

    private static Logger log = LoggerFactory.getLogger(CapitalValidator.class);

    public void init(final FormState formState) {
        rootCountryId = formState.getObjects().getRootDomainObject().getId();
    }

    @Override
    public ValidationResult validate(Dto dtoToValidate) {
        ValidationResult validationResult = new ValidationResult();
        if (rootCountryId == null) {
            log.error("CapitalValidator is not properly initialized -> ignoring it.");
            return validationResult;
        }
        if (dtoToValidate instanceof LinkEditingWidgetState) {
            LinkEditingWidgetState state = (LinkEditingWidgetState)dtoToValidate;
            ArrayList<Id> ids =  state.getIds();
            if (ids != null && ids.size() == 1) {
                Id cityId = ids.get(0);
                CrudService crudService = getCrudService();
                DomainObject city = crudService.find(cityId);
                if (city != null) {
                    if (city.getReference("country") != null && !city.getReference("country").equals(rootCountryId)) {
                        validationResult.addError("Столица принадлежит другому государству!");
                    }
                }
            }
        }
        return validationResult;
    }

    private CrudService getCrudService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(CrudService.class);
    }
}
