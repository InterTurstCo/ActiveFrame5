package ru.intertrust.cm.core.gui.api.client.history;

/**
 *
 * Marker of exception from restore history data.
 * @author Sergey.Okolot
 *         Created on 11.07.2014 17:39.
 */
public class HistoryException extends RuntimeException {

    public HistoryException() {
    }

    public HistoryException(String message) {
        super(message);
    }

    public HistoryException(Throwable e) {
        super(e);
    }
}
