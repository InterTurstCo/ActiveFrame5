package ru.intertrust.cm.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "shutdown")
public class Shutdown extends org.jboss.as.plugin.server.Shutdown{

}
