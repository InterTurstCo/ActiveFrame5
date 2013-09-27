package ru.intertrust.cm.core.model;

/**
 * Клас исключения, формирующееся в подсистеме точек расширения
 * 
 * @author larin
 * 
 */
public class ExtensionPointException extends SystemException {

	private static final long serialVersionUID = 3814955429458773118L;

	public ExtensionPointException() {
	}

	public ExtensionPointException(String message) {
		super(message);
	}

	public ExtensionPointException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtensionPointException(Throwable cause) {
		super(cause);
	}

	public ExtensionPointException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
