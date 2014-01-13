package ru.intertrust.cm.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.plugin.cli.ExecuteCommands;

@Mojo(name = "execute-commands-ex", threadSafe = true)
public class ExecuteCommandsEx extends ExecuteCommands {

    /**
     * The commands to execute.
     */
    @Parameter(alias = "execute-commands", required = true)
    private CommandsEx executeCommands;

    @Override
    public String goal() {
        return "execute-commands-ex";
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Executing commands");
        synchronized (CLIENT_LOCK) {
            final ModelControllerClient client = getClient();
            try {
                executeCommands.executeEx(client);
            } catch (IOException e) {
                throw new MojoFailureException("Could not execute commands.", e);
            } finally {
                close();
            }
        }
    }}
