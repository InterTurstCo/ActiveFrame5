package ru.intertrust.cm.core.config.gui.action;

import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import static org.junit.Assert.*;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 12:44.
 */
public class ToolBarConfigTest {

    private static final String NAME = "id_value";
    private static final String COMPONENT_NAME = "action.tool.bar";
    private static final String STYLE_CLASS = "styleClass_value";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetActionConfigs() throws Exception {
        final InputStream is = getClass().getResourceAsStream("tool-bar-junit.xml");
        final Serializer serializer = new Persister();
        final ToolBarConfig tbConfig = serializer.read(ToolBarConfig.class, is);
        assertEquals(NAME, tbConfig.getName());
        assertEquals(COMPONENT_NAME, tbConfig.getComponentName());
        assertEquals(STYLE_CLASS, tbConfig.getStyleClass());
        assertTrue(tbConfig.isUseDefault());
        assertNotNull(tbConfig);
    }
}
