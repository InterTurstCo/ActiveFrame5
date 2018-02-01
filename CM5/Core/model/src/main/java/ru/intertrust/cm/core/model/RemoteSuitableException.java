package ru.intertrust.cm.core.model;

import java.lang.reflect.Field;

public class RemoteSuitableException extends RollingBackException {

    private static final Field fCause = (new Object() {

        Field get () {
            try {
                final Field fCause = Throwable.class.getDeclaredField("cause");
                fCause.setAccessible(true);
                return fCause;
            } catch (final NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

    }).get();

    public static RuntimeException convert (final Exception e) {
        
        final Throwable throwable = processCausedChain(e);
        
        if (throwable instanceof RuntimeException) {
            return (RuntimeException)throwable;
        } else {
            final RuntimeException result = new FatalException(throwable);
            final StackTraceElement[] st1 = result.getStackTrace();
            final StackTraceElement[] st2 = new StackTraceElement[st1.length - 1];
            System.arraycopy(st1, 1, st2, 0, st2.length);
            result.setStackTrace(st2);
            return result;
        }
        
    }

    private static Throwable processCausedChain (final Throwable throwable) {

        final Throwable causeOrig = throwable.getCause();
        final Throwable causeProcessed = (causeOrig == null) ? null : processCausedChain(causeOrig);

        if (!throwable.getClass().getName().startsWith("java.lang.") && !(throwable instanceof SystemException)) {
            return new RemoteSuitableException(throwable, causeProcessed);
        } else if (causeOrig != causeProcessed) {
            try {
                fCause.set(throwable, causeProcessed);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return throwable;

    }

    private RemoteSuitableException (final Throwable throwable, final Throwable cause) {
        super(throwable.getMessage(), cause);
        this.setStackTrace(throwable.getStackTrace());
        this.origClassName = throwable.getClass().getName();
    }

    private final String origClassName;

    @Override
    public String toString () {
        final String message = this.getLocalizedMessage();
        return (message != null) ? this.origClassName + ": " + message : this.origClassName;
    }

}