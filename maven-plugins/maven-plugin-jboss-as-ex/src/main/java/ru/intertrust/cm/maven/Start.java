package ru.intertrust.cm.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "start", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class Start extends org.jboss.as.plugin.server.Start {

}
