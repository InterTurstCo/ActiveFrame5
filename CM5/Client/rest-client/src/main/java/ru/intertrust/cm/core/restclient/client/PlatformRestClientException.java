package ru.intertrust.cm.core.restclient.client;

public class PlatformRestClientException extends RuntimeException{
    public PlatformRestClientException(String message) {
        super(message);
    }

    public PlatformRestClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
