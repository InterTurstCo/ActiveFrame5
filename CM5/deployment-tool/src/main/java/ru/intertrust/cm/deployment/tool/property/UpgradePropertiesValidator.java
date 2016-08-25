package ru.intertrust.cm.deployment.tool.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
@Component
public class UpgradePropertiesValidator {

    private static Logger logger = LoggerFactory.getLogger(UpgradePropertiesValidator.class);

    @Autowired
    private Validator validator;

    @Autowired
    private UpgradeProperties properties;

    public boolean validate() {
        Set<ConstraintViolation<UpgradeProperties>> validate = validator.validate(properties);
        boolean hasErrors = !CollectionUtils.isEmpty(validate);
        if (hasErrors) {
            logger.error("Please check update.properties file");
            for (ConstraintViolation<UpgradeProperties> violation : validate) {
                Path property = violation.getPropertyPath();
                String message = violation.getMessage();
                logger.error("SystemProperty {} - {}", property, message);
            }
            logger.error("Some properties is empty", new IllegalStateException());
            return false;
        }
        return true;
    }
}
