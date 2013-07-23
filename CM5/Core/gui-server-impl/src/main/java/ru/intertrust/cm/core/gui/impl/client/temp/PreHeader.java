package ru.intertrust.cm.core.gui.impl.client.temp;

import com.vaadin.annotations.Theme;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;

import java.io.File;

@Theme("mytheme")
public class PreHeader {

	public Component render() {
		String basepath = VaadinService.getCurrent().getBaseDirectory()
				.getAbsolutePath();
		FileResource resource = new FileResource(new File(basepath
				+ "/WEB-INF/images/CompanyMedia-small.gif"));
		Image image = new Image("", resource);
			return image;
	}

	public AbstractComponent getPreHeader() {
		HorizontalLayout horizont = new HorizontalLayout();
		horizont.addComponent(render());
		horizont.setSizeFull();
		return horizont;
	}

}
