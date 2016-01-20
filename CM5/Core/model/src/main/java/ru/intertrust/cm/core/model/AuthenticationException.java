package ru.intertrust.cm.core.model;

/**
 * Исключение, возникающее при неудачной аутентификации пользователя системы
 * @author Denis Mitavskiy
 *         Date: 10.07.13
 *         Time: 15:18
 */
public class AuthenticationException extends NonRollingBackException {
    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
