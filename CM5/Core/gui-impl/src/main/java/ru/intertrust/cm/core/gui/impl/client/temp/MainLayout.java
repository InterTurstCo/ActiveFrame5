package ru.intertrust.cm.core.gui.impl.client.temp;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class MainLayout {
	static HorizontalLayout preheader;
	static HorizontalLayout header;
	
	public static void addHeaderToMainLayoutPage(HorizontalLayout headerRender){
		header = headerRender; 
	}
	
	public static void addPreHeaderToMainLayoutPage(HorizontalLayout preheaderRender){
		preheader = preheaderRender; 
	}
	
	public static VerticalLayout renderMainLayout() {
		//stub
		Panel tpanel = new Panel();
		tpanel.setSizeFull();
		
		Panel tpanel2 = new Panel();
		tpanel2.setSizeFull();
		//end stub
		
		
		
		
		VerticalLayout rootVerticalLayout = new VerticalLayout();
		rootVerticalLayout.setSizeFull();
		
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		
		
		horizontalLayout.setSizeFull();
		
		
		//add preheader
		HorizontalLayout preHeaderHorizontalLayout = new HorizontalLayout();
		rootVerticalLayout.addComponent(preHeaderHorizontalLayout);
		preHeaderHorizontalLayout.setSizeFull();
		preHeaderHorizontalLayout.setHeight("60px");
		PreHeader.render(preHeaderHorizontalLayout);
		//add header
		HorizontalLayout headerHorizontalLayout = new HorizontalLayout();
		headerHorizontalLayout.setSizeFull();
		rootVerticalLayout.addComponent(headerHorizontalLayout);
		headerHorizontalLayout.setHeight("30px");
		Header.basic(headerHorizontalLayout);
		
		
		//navigation + content
		rootVerticalLayout.addComponent(horizontalLayout);

		rootVerticalLayout.setExpandRatio(horizontalLayout, 1);
		
		VerticalLayout firstBodyVerticalLayout = new VerticalLayout();
		firstBodyVerticalLayout.setSizeFull();
		firstBodyVerticalLayout.setWidth("70px");
		firstBodyVerticalLayout.addComponent(tpanel);
		horizontalLayout.addComponent(firstBodyVerticalLayout);
		
		VerticalLayout secondBodyVerticalLayout = new VerticalLayout();
		secondBodyVerticalLayout.setSizeFull();
		secondBodyVerticalLayout.setWidth("130px");
		
		secondBodyVerticalLayout.addComponent(tpanel2);
		horizontalLayout.addComponent(secondBodyVerticalLayout);
		
		Panel rightPanel = new Panel();

		
		horizontalLayout.addComponent(rightPanel);
		
		horizontalLayout.setExpandRatio(rightPanel, 5);
		
		rightPanel.setSizeFull();
		
			//for tests	
		VerticalSplitPanel rightVerticalSplitPanel = new VerticalSplitPanel();
		rightPanel.setContent(rightVerticalSplitPanel);
		return  rootVerticalLayout;
	}
}
