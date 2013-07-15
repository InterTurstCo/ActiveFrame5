package ru.intertrust.cm.core.gui.impl.client.temp;

import com.vaadin.ui.AbstractComponent;

public class ContentManagment {

	private AbstractComponent preHeader;
	private AbstractComponent header;
	private AbstractComponent leftNavigationSection;
	private AbstractComponent centerNavigationSection;

	public ContentManagment() {
		this.preHeader = new PreHeader().getPreHeader();
		this.header = new Header().getHeader();
		this.leftNavigationSection = new LeftNavigationSection().render();
		this.centerNavigationSection = new CenterNavigationSection().render();
		
	}

	public AbstractComponent getPreHeader() {
		return preHeader;
	}

	public void setPreHeader(PreHeader preHeader) {
		this.preHeader = preHeader.getPreHeader();
	}

	public AbstractComponent getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header.getHeader();
	}

	public AbstractComponent getLeftNavigationSection() {
		return leftNavigationSection;
	}

	public void setLeftNavigationSection(AbstractComponent leftNavigationSection) {
		leftNavigationSection.setSizeFull();
		this.leftNavigationSection = leftNavigationSection;
	}

	public AbstractComponent getCenterNavigationSection() {
		return centerNavigationSection;
	}

	public void setCenterNavigationSection(
			AbstractComponent centerNavigationSection) {
		centerNavigationSection.setSizeFull();
		this.centerNavigationSection = centerNavigationSection;
	}

}
