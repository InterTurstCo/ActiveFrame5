package ru.intertrust.cm.core.business.api;

/**
 * Контекст, передаваемый скрипту
 * @author atsvetkov
 *
 */
public interface ScriptContext {

    Object getResult();

    void setResult(Object result);

}
