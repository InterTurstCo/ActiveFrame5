package ru.intertrust.cm.core.dao.impl.attach;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.config.DeleteFileConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDeleteAttachmentStrategyFactoryTest {

    @Mock
    private FileSystemAttachmentStorageHelper helper;
    @Mock
    private DeleteStrategyCreator neverStrategyCreator;
    @Mock
    private DeleteStrategyCreator delayedStrategyCreator;
    @Mock
    private FileDeleteStrategy neverDeleteStrategy;

    private FileDeleteStrategy delayedDeleteStrategy;

    private DefaultDeleteAttachmentStrategyFactory factory;

    @Before
    public void init() {
        delayedDeleteStrategy = new DelayedFileDeleteStrategy();
        factory = new DefaultDeleteAttachmentStrategyFactory(helper);

        when(neverStrategyCreator.getType()).thenReturn(DeleteFileConfig.Mode.NEVER);
        when(delayedStrategyCreator.getType()).thenReturn(DeleteFileConfig.Mode.DELAYED);

        when(neverStrategyCreator.create()).thenReturn(neverDeleteStrategy);
        when(delayedStrategyCreator.create()).thenReturn(delayedDeleteStrategy);

        factory.init(Arrays.asList(neverStrategyCreator, delayedStrategyCreator));
    }

    // Без конфига и без пропертей - NEVER
    @Test
    public void createDeleteStrategy_NEVER_WithoutConfig_WithoutProps() {
        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", null);
        assertSame(strategy, neverDeleteStrategy);
    }

    // Создание NEVER через конфиг
    @Test
    public void createDeleteStrategy_NEVER_Config() {
        final DeleteFileConfig config = new DeleteFileConfig();
        config.setMode(DeleteFileConfig.Mode.NEVER);
        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", config);

        assertSame(strategy, neverDeleteStrategy);
    }


    // Создание NEVER с null конфигом, но с пропертями

    @Test
    public void createDeleteStrategy_NEVER_Props() {
        when(helper.getProperty("mode", "A")).thenReturn("never");
        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", null);

        assertSame(strategy, neverDeleteStrategy);
    }

    // Создание NEVER с конфигом переопределенным пропертями
    @Test
    public void createDeleteStrategy_NEVER_Config_And_Props() {
        final DeleteFileConfig config = new DeleteFileConfig();
        config.setMode(DeleteFileConfig.Mode.DELAYED);

        when(helper.getProperty("mode", "A")).thenReturn("never");
        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", config);

        assertSame(strategy, neverDeleteStrategy);
    }

    // Создание Delayed с полным конфигом
    @Test
    public void createDeleteStrategy_DELAYED_Config() {
        final DeleteFileConfig config = new DeleteFileConfig();
        config.setMode(DeleteFileConfig.Mode.DELAYED);
        config.setDelay(5);

        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", config);

        assertSame(strategy, delayedDeleteStrategy);
        DeleteFileConfig actual = (DeleteFileConfig) ReflectionTestUtils.getField(delayedDeleteStrategy, "config");

        assertEquals(config.getDelay(), actual.getDelay());
        assertEquals(config.getMode(), actual.getMode());
    }

    // Создание Delayed с дефолтным значением с конфигом
    @Test
    public void createDeleteStrategy_DELAYED_Config_WithDefaultDelayValue() {
        final DeleteFileConfig config = new DeleteFileConfig();
        config.setMode(DeleteFileConfig.Mode.DELAYED);

        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", config);

        assertSame(strategy, delayedDeleteStrategy);
        DeleteFileConfig actual = (DeleteFileConfig) ReflectionTestUtils.getField(delayedDeleteStrategy, "config");

        assertNotNull(actual.getDelay());
        assertEquals(1, (int) actual.getDelay());
        assertEquals(config.getMode(), actual.getMode());
    }

    // Создание Delayed без конфига, но с пропертями (полными)
    @Test
    public void createDeleteStrategy_DELAYED_Props() {

        when(helper.getProperty("mode", "A")).thenReturn("delayed");
        when(helper.getProperty("delay", "A")).thenReturn("5");

        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", null);

        assertSame(strategy, delayedDeleteStrategy);
        DeleteFileConfig actual = (DeleteFileConfig) ReflectionTestUtils.getField(delayedDeleteStrategy, "config");

        assertNotNull(actual.getDelay());
        assertEquals(5, (int) actual.getDelay());
        assertEquals(DeleteFileConfig.Mode.DELAYED, actual.getMode());
    }

    // Создание Delayed без конфига, с пропертями с дефолтом
    @Test
    public void createDeleteStrategy_DELAYED_Props_withDefaultDelayValue() {

        when(helper.getProperty("mode", "A")).thenReturn("delayed");

        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", null);

        assertSame(strategy, delayedDeleteStrategy);
        DeleteFileConfig actual = (DeleteFileConfig) ReflectionTestUtils.getField(delayedDeleteStrategy, "config");

        assertNotNull(actual.getDelay());
        assertEquals(1, (int) actual.getDelay());
        assertEquals(DeleteFileConfig.Mode.DELAYED, actual.getMode());
    }

    // Создание Delayed, который переопределяет не delayed c дефолтом
    @Test
    public void createDeleteStrategy_DELAYED_Override_NEVER_WithDefaultValue() {

        final DeleteFileConfig config = new DeleteFileConfig();
        config.setMode(DeleteFileConfig.Mode.NEVER);

        when(helper.getProperty("mode", "A")).thenReturn("delayed");

        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", config);

        assertSame(strategy, delayedDeleteStrategy);
        DeleteFileConfig actual = (DeleteFileConfig) ReflectionTestUtils.getField(delayedDeleteStrategy, "config");

        assertNotNull(actual.getDelay());
        assertEquals(1, (int) actual.getDelay());
        assertEquals(DeleteFileConfig.Mode.DELAYED, actual.getMode());
    }

    // Создание Delayed, который переопределяет не delayed с полным конфигом
    @Test
    public void createDeleteStrategy_DELAYED_Override_NEVER() {

        final DeleteFileConfig config = new DeleteFileConfig();
        config.setMode(DeleteFileConfig.Mode.NEVER);

        when(helper.getProperty("mode", "A")).thenReturn("delayed");
        when(helper.getProperty("delay", "A")).thenReturn("5");

        final FileDeleteStrategy strategy = factory.createDeleteStrategy("A", config);

        assertSame(strategy, delayedDeleteStrategy);
        DeleteFileConfig actual = (DeleteFileConfig) ReflectionTestUtils.getField(delayedDeleteStrategy, "config");

        assertNotNull(actual.getDelay());
        assertEquals(5, (int) actual.getDelay());
        assertEquals(DeleteFileConfig.Mode.DELAYED, actual.getMode());
    }

}
