package ru.intertrust.cm.core.client;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.Configuration;

import java.io.File;
import java.io.StringWriter;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 6:43 PM
 */
public class Hello extends UI {
    //@EJB
    //private TestEjb myEjb;

    public static String getConfigXml() {
        Serializer serializer = new Persister();
        File source = new File("F:\\intertrust\\git\\CM5SRC\\CM5\\Core\\src\\main\\resources\\config\\business-objects.xml");

        try {
            Configuration example = serializer.read(Configuration.class, source);
            StringWriter stringWriter = new StringWriter();
            serializer.write(example, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        Page.getCurrent().setTitle("Test");
        // The root of the component hierarchy
        VerticalLayout mainScreenContent = new VerticalLayout();
        mainScreenContent.setSizeFull(); // Use entire window
        setContent(mainScreenContent);   // Attach to the UI

        // Add some component
        Label heading = new Label("<b>This is a header!</b>");
        heading.setContentMode(ContentMode.HTML);
        heading.setHeight(40, Unit.PIXELS);
        mainScreenContent.addComponent(heading);

        Tree tree = new Tree("My Tree", createTreeContent());
        tree.setWidth("100px");


        final VerticalSplitPanel activityContent = buildCollectionActivity("Table 1...");

        // Layout inside layout
        final HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
        horizontalSplitPanel.setSplitPosition(20, Unit.PERCENTAGE);
        horizontalSplitPanel.setSizeFull(); // Use all available space
        // Couple of horizontally laid out components
        horizontalSplitPanel.addComponent(tree);
        horizontalSplitPanel.addComponent(activityContent);

        tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                Notification.show("Tree Click!", "Wow", Notification.Type.WARNING_MESSAGE);
                horizontalSplitPanel.setSecondComponent(buildCollectionActivity("Table 2....!!!"));
                Notification.show("Substituted!", "Wow", Notification.Type.WARNING_MESSAGE);
            }
        });
        //hor.setExpandRatio(table, 1); // Expand to fill

        mainScreenContent.addComponent(horizontalSplitPanel);
        mainScreenContent.setExpandRatio(horizontalSplitPanel, 1); // Expand to fill
    }

    private VerticalSplitPanel buildCollectionActivity(String name) {
        Table table = new Table(name, generateContent(name));
        table.setSizeFull();
        table.addItemClickListener( new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                Notification.show("This is the caption",
                        "This is the description",
                        Notification.Type.WARNING_MESSAGE);
            }
        });

        final VerticalSplitPanel activityContent = new VerticalSplitPanel();
        activityContent.addComponent(table);
        return activityContent;
    }

    private Table generateContent(String name) {
        final Table table = new Table("The Brightest Stars");

        // Define two columns for the built-in container
        table.addContainerProperty("Name", String.class, null);
        table.addContainerProperty("Mag", Float.class, null);

        // Add a row the hard way
        Object newItemId = table.addItem();
        Item row1 = table.getItem(newItemId);
        row1.getItemProperty("Name").setValue(name);
        row1.getItemProperty("Mag").setValue(-1.46f);

        // Add a few other rows using shorthand addItem()
        table.addItem(new Object[]{"Canopus", -0.72f}, 2);
        table.addItem(new Object[]{"Arcturus", -0.04f}, 3);
        table.addItem(new Object[]{"Alpha Centauri", -0.01f}, 4);

        // Show 5 rows
        table.setPageLength(5);
        return table;
    }

    private Container createTreeContent() {
        final HierarchicalContainer container =
                new HierarchicalContainer();
        container.addItem("One Node");
        container.addItem("Another Node");
        container.addItem("Third Node");
        return container;
    }
}
