package ru.intertrust.cm.core.gui.impl.client.temp;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class MainLayout {

	public MainLayout() {
		
	}

	public VerticalLayout renderMainLayout(ContentManagment content) {

		VerticalLayout rootVerticalLayout = new VerticalLayout();
		rootVerticalLayout.setSizeFull();

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		// add preheader
		HorizontalLayout preHeaderHorizontalLayout = new HorizontalLayout();
		rootVerticalLayout.addComponent(preHeaderHorizontalLayout);
		preHeaderHorizontalLayout.setSizeFull();
		preHeaderHorizontalLayout.setHeight("60px");
		preHeaderHorizontalLayout.addComponent(content.getPreHeader());

		// add header
		HorizontalLayout headerHorizontalLayout = new HorizontalLayout();
		headerHorizontalLayout.setSizeFull();
		rootVerticalLayout.addComponent(headerHorizontalLayout);
		headerHorizontalLayout.setHeight("30px");
		headerHorizontalLayout.addComponent(content.getHeader());

		// navigation + content
		rootVerticalLayout.addComponent(horizontalLayout);

		rootVerticalLayout.setExpandRatio(horizontalLayout, 1);

		VerticalLayout firstBodyVerticalLayout = new VerticalLayout();
		firstBodyVerticalLayout.setSizeFull();
		firstBodyVerticalLayout.setWidth("70px");
		firstBodyVerticalLayout
				.addComponent(content.getLeftNavigationSection());
		horizontalLayout.addComponent(firstBodyVerticalLayout);

		VerticalLayout secondBodyVerticalLayout = new VerticalLayout();
		secondBodyVerticalLayout.setSizeFull();
		secondBodyVerticalLayout.setWidth("130px");

		secondBodyVerticalLayout.addComponent(content
				.getCenterNavigationSection());
		horizontalLayout.addComponent(secondBodyVerticalLayout);

		Panel rightPanel = new Panel();

		horizontalLayout.addComponent(rightPanel);

		horizontalLayout.setExpandRatio(rightPanel, 5);

		rightPanel.setSizeFull();

		// vertical split panel
		VerticalSplitPanel rightVerticalSplitPanel = new VerticalSplitPanel();
		rightPanel.setContent(rightVerticalSplitPanel);
		return rootVerticalLayout;
	}
}
