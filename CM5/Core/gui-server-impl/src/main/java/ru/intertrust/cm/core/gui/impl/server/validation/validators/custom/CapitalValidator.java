package ru.intertrust.cm.core.gui.impl.server.validation.validators.custom;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.ServerValidator;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestBoxState;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Пример кастомного серверного валидатора. Проверяет, что столица не является городом другой страны.
 * @author Lesia Puhova
 *         Date: 10.03.14
 *         Time: 13:58
 */
public class CapitalValidator implements ServerValidator {

    private Id rootCountryId;

    private static Logger log = LoggerFactory.getLogger(CapitalValidator.class);

    public void init(ActionContext context) {
        rootCountryId = context.getRootObjectId();
    }

    @Override
    public ValidationResult validate(Dto dtoToValidate) {
        ValidationResult validationResult = new ValidationResult();
        if (rootCountryId == null) {
            log.error("CapitalValidator is not properly initialized -> ignoring it.");
            return validationResult;
        }
        if (dtoToValidate instanceof SuggestBoxState) {
            SuggestBoxState state = (SuggestBoxState)dtoToValidate;
            ArrayList<Id> ids =  state.getIds();
            if (ids != null && ids.size() == 1) {
                Id cityId = ids.get(0);
                CrudService crudService = getCrudService();
                DomainObject city = crudService.find(cityId);
                if (city != null) {
                    List<DomainObject> countries = crudService.findLinkedDomainObjects(cityId, "country", "capital");
                    if (countries != null && countries.size() == 1) {
                        DomainObject country = countries.get(0);
                        if (country.getId() != null && !country.getId().equals(rootCountryId)) {
                            validationResult.addError("Capital belongs to another country!");
                        }
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
