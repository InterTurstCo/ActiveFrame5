package ru.intertrust.cm.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.plugin.cli.Commands;

public class CommandsEx extends Commands {
    /**
     * The CLI commands to execute.
     */
    @Parameter
    private List<String> commands = new ArrayList<String>();

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
        for (String cmd : commands) {
            try {
                client.execute(ctx.buildRequest(cmd));
            } catch (CommandFormatException e) {
                throw new IllegalArgumentException(String.format("Command '%s' is invalid", cmd), e);
            } catch (CommandLineException e) {
                //Игнорируем ошибку
            }
        }
    }
}
