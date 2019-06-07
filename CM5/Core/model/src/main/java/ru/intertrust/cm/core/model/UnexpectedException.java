package ru.intertrust.cm.core.model;

public abstract class UnexpectedException extends RollingBackException {
    
    protected UnexpectedException () {
    }

    protected UnexpectedException (String msg, Throwable e) {
        super(msg,e);
    }

    protected UnexpectedException (String msg) {
        super(msg);
    }
}