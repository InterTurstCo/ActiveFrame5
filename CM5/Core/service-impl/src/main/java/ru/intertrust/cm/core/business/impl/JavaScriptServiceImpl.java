package ru.intertrust.cm.core.business.impl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ru.intertrust.cm.core.business.api.ScriptContext;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.model.EventTriggerException;
import ru.intertrust.cm.core.tools.Session;

/**
 * 
 * @author atsvetkov
 *
 */
public class JavaScriptServiceImpl implements ScriptService {

    private static final String SESSION_OBJECT = "session";

    private static final String CONTEXT_OBJECT = "ctx";

    @Override
    public Object eval(String script, ScriptContext context) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        engine.put(SESSION_OBJECT, new Session());
        engine.put(CONTEXT_OBJECT, context);
        try {
            engine.eval(script);
            return context.getResult();
        } catch (ScriptException e) {
            throw new EventTriggerException("Error executing script " + script);
        }
    }

}
