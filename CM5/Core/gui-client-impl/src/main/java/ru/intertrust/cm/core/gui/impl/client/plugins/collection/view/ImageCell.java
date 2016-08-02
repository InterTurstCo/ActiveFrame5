package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 14/02/14
 *         Time: 12:05 PM
 */
public class ImageCell extends AbstractCell<String> {
    private String imageWidth;
    private String imageHeight;

    public ImageCell(String imageWidth, String imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String imagePath, SafeHtmlBuilder sb) {
        StringBuilder html = new StringBuilder("<div/>");
	if(imagePath != null && !imagePath.trim().isEmpty()){
		html.append("<img width=");	
        	html.append(imageWidth);
        	html.append(" height=");
        	html.append(imageHeight);
        	html.append("; src=" + imagePath + ">");
	}
	html.append("</div>");	
        sb.append(SafeHtmlUtils.fromTrustedString(html.toString()));
    }

}
