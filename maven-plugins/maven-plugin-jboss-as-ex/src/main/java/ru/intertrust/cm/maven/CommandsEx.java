package ru.intertrust.cm.maven;

import java.io.IOException;

import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.plugin.cli.Commands;
import org.jboss.as.plugin.common.Operations;
import org.jboss.dmr.ModelNode;

public class CommandsEx extends Commands {

    @Parameter(defaultValue = "false")
    private boolean ignoreException;

    public final void executeEx(final ModelControllerClient client) throws IOException {

        if (ignoreException) {
            final CommandContext ctx = create(client);
            try {

                executeCommandsEx(client, ctx);

            } finally {
                ctx.terminateSession();
                ctx.bindClient(null);
            }

        } else {
            execute(client);
        }
    }

    private void executeCommandsEx(final ModelControllerClient client, final CommandContext ctx) throws IOException {
        for (String cmd : getCommands()) {
            final ModelNode result;
            try {
                result = client.execute(ctx.buildRequest(cmd));
            } catch (CommandFormatException e) {
                throw new IllegalArgumentException(String.format("Command '%s' is invalid", cmd), e);
            }
            if (!Operations.successful(result)) {
                //Игнорируем ошибку
            }
        }
    }
}
