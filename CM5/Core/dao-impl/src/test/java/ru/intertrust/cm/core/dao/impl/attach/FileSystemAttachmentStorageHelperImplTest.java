package ru.intertrust.cm.core.dao.impl.attach;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemAttachmentStorageHelperImplTest {

    @Mock
    private Environment env;

    private FileSystemAttachmentStorageHelperImpl helper;

    @Before
    public void init() {
        helper = new FileSystemAttachmentStorageHelperImpl(env);
    }

    @Test
    public void getProperty() {
        when(env.getProperty("attachments.storage.B.A")).thenReturn("VALUE");

        final String property = helper.getProperty("A", "B");
        assertEquals("VALUE", property);
    }

    @Test
    public void getProperty_WithoutPropertyForBType() {
        when(env.getProperty("attachments.storage.A")).thenReturn("VALUE");

        final String property = helper.getProperty("A", "B");
        assertEquals("VALUE", property);
    }

    @Test
    public void getPureProperty() {
        when(env.getProperty("A")).thenReturn("VALUE");

        final String property = helper.getPureProperty("A");
        assertEquals("VALUE", property);
    }

}
