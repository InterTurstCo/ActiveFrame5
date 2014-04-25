package ru.intertrust.cm.core.business.api;

/**
 * Контекст, передаваемый скрипту
 * @author atsvetkov
 *
 */
public interface ScriptContext {

    Boolean getResult();

    void setResult(Boolean result);

}
