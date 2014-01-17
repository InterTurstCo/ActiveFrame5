package ru.intertrust.cm.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "execute-commands", threadSafe = true)
public class ExecuteCommands extends org.jboss.as.plugin.cli.ExecuteCommands {

}
