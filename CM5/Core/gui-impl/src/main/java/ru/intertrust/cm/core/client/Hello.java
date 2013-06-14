package ru.intertrust.cm.core.client;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.model.BusinessObjectsConfiguration;

import java.io.File;
import java.io.StringWriter;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 6:43 PM
 */
@Title("hello")
public class Hello extends UI {
    @Override
    protected void init(VaadinRequest request) {
        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        setContent(splitPanel);

        /* Build the component tree */
        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.addComponent(new Label(getConfigXml(), ContentMode.PREFORMATTED));
        splitPanel.addComponent(leftLayout);

        HorizontalLayout bottomLeftLayout = new HorizontalLayout();
        leftLayout.addComponent(bottomLeftLayout);


        /* Set the contents in the left of the split panel to use all the space */
        leftLayout.setSizeFull();

        bottomLeftLayout.setWidth("100%");
    }

    public static String getConfigXml() {
        Serializer serializer = new Persister();
        File source = new File("c:\\projects\\CM5_workspace\\CM5\\Core\\src\\main\\resources\\config\\business-objects.xml");

        try {
            BusinessObjectsConfiguration example = serializer.read(BusinessObjectsConfiguration.class, source);
            StringWriter stringWriter = new StringWriter();
            serializer.write(example, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
