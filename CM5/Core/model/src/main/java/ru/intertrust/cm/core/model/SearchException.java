package ru.intertrust.cm.core.model;

public class SearchException extends NonRollingBackException {

    public SearchException() {
        super();
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchException(String message) {
        super(message);
    }

    public SearchException(Throwable cause) {
        super(cause);
    }

}
