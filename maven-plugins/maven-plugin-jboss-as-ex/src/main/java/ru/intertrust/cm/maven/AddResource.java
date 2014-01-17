package ru.intertrust.cm.maven;

import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "add-resource", threadSafe = true)
public class AddResource extends org.jboss.as.plugin.deployment.resource.AddResource{

}
