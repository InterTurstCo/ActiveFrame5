package ru.intertrust.cm.core.model;

/**
 * Клас исключения, формирующееся в подсистеме workflow
 * @author larin
 *
 */
public class ProcessException extends SystemException{

	private static final long serialVersionUID = 3814955429458773118L;

	public ProcessException() {
    }

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessException(Throwable cause) {
        super(cause);
    }

    public ProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
