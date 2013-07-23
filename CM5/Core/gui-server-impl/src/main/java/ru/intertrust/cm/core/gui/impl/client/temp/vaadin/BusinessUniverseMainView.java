package ru.intertrust.cm.core.gui.impl.client.temp.vaadin;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import ru.intertrust.cm.core.gui.impl.client.temp.ContentManagment;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.13
 *         Time: 16:41
 */
public class BusinessUniverseMainView extends VerticalLayout implements View {
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        addComponent(new Label("Good day"));
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
