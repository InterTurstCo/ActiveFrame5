package ru.inttrust.workflow.runawfe.handler;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.restclient.client.PlatformClient;
import ru.inttrust.workflow.runawfe.impl.Session;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.VariableDefinition;

public class JavaScriptActionHandler extends ActionHandlerBase {

    @Autowired
    private PlatformClient af5Client;

    @Autowired
    private SwimlaneDao swimlaneDao;

    @Autowired
    private ExecutorDao executorDao;

    @Override
    public void execute(ExecutionContext executionContext) throws ScriptException {
        List<VariableDefinition> rawDefinitions = Lists.newArrayList();
        for (VariableDefinition definition : executionContext.getProcessDefinition().getVariables()) {
            if (definition.isUserType()) {
                rawDefinitions.addAll(definition.expandUserType(false));
            } else {
                rawDefinitions.add(definition);
            }
        }
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        for (VariableDefinition definition : rawDefinitions) {
            Object value = executionContext.getVariableValue(definition.getName());
            if (value != null) {
                engine.put(definition.getScriptingName(), value);
            }
        }

        engine.put("af5", af5Client);
        engine.put("session", new Session(executionContext, swimlaneDao, executorDao));

        engine.eval(configuration);
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        for (VariableDefinition definition : rawDefinitions) {
            Object value = bindings.get(definition.getScriptingName());
            Object currentValue = executionContext.getVariableValue(definition.getName());
            if (value != null && !Objects.equal(value, currentValue)) {
                executionContext.setVariableValue(definition.getName(), value);
            }
        }
    }
}

