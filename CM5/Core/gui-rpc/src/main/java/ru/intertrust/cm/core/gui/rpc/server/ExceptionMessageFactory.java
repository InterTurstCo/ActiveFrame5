package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.model.SystemException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Sergey.Okolot
 *         Created on 11.09.2014 14:14.
 */
public final class ExceptionMessageFactory {

    private static final String GUI_EXCEPTION_KEY = "GuiException";
    private static final String COMMAND_NAME_PLACEHOLDER = "commandName";

    private static final String ACCESS_EXCEPTION = "AccessException";
    private static final String AUTHENTICATION_EXCEPTION = "AuthenticationException";
    private static final String OPTIMISTIC_LOCK_EXCEPTION = "OptimisticLockException";
    private static final String PERMISSION_EXCEPTION = "PermissionException";

    private ExceptionMessageFactory() {
    }

    private static final Set<String> NOT_LOGGED_EXCEPTIONS = new HashSet<>();
    static {
        NOT_LOGGED_EXCEPTIONS.add(ACCESS_EXCEPTION);
        NOT_LOGGED_EXCEPTIONS.add(AUTHENTICATION_EXCEPTION);
        NOT_LOGGED_EXCEPTIONS.add(OPTIMISTIC_LOCK_EXCEPTION);
        NOT_LOGGED_EXCEPTIONS.add(PERMISSION_EXCEPTION);
    }

    public static Pair<String, Boolean> getMessage(final Command command, Throwable ex, String currentLocale) {
        SystemException cause = ex instanceof SystemException ? (SystemException) ex : null;
        StringBuilder msgBuilder = new StringBuilder();
        while (ex.getCause() != null) {
            ex = ex.getCause();
            if (ex instanceof SystemException) {
                cause = (SystemException) ex;
            }
        }
        String exceptionKey = null;
        String message = null;
        if (cause != null) {
            exceptionKey = cause.getClass().getSimpleName();
            if (GUI_EXCEPTION_KEY.equals(exceptionKey)) {
                return new Pair<>(cause.getMessage(), !NOT_LOGGED_EXCEPTIONS.contains(exceptionKey));
            }
            message = MessageResourceProvider.getMessage(exceptionKey, currentLocale, null);
        }
        if (message == null) {
            message = MessageResourceProvider.getMessage(LocalizationKeys.SYSTEM_EXCEPTION,
                    currentLocale,"Системная ошибка при выполнении ${commandName}, обратитесь к администратору.");
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(COMMAND_NAME_PLACEHOLDER, command.getName());
        message = PlaceholderResolver.substitute(message, placeholders);
        msgBuilder.append(message);
        if(cause.getMessage()!=null){
            msgBuilder.append(System.getProperty("line.separator")).append(cause.getMessage());
        }
        return new Pair<>(msgBuilder.toString(), !NOT_LOGGED_EXCEPTIONS.contains(exceptionKey));
   }
}
