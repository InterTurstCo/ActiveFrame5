package ru.intertrust.cm.core.gui.impl.client.temp;

import java.io.File;

import com.vaadin.annotations.Theme;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;
@Theme("mytheme")
public class PreHeader {
	
	public static void render(HorizontalLayout layout) {

		// Find the application directory
		String basepath = VaadinService.getCurrent()
		                  .getBaseDirectory().getAbsolutePath();

		// Image as a file resource
		FileResource resource = new FileResource(new File(basepath +
		                        "/WEB-INF/images/CompanyMedia-small.gif"));

		// Show the image in the application
		Image image = new Image("", resource);
		        
		// Let the user view the file in browser or download it
		//Link link = new Link("Link to the image file", resource);
		
		//height = (int)image.getHeight();
		layout.addComponent(image);
	}
	
}
