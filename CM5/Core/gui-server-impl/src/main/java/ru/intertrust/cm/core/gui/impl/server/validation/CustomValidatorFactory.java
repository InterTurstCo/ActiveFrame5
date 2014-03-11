package ru.intertrust.cm.core.gui.impl.server.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.ServerValidator;

/**
 * @author Lesia Puhova
 *         Date: 10.03.14
 *         Time: 14:05
 */
public class CustomValidatorFactory {

    private static Logger log = LoggerFactory.getLogger(CustomValidatorFactory.class);

    private CustomValidatorFactory() {} // non-instantiable

    public static ServerValidator createInstance(String className, String widgetName) {
        try {
            Class<ServerValidator> validatorClass = ( Class<ServerValidator>) Class.forName(className);
            return validatorClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error(String.format("Custom validator '%s' cannot bu instantiated", className), e);
        }
        return null;
    }
}
