package ru.intertrust.cm.core.config.form.extension;

import org.junit.Assert;
import org.junit.Test;
import ru.intertrust.cm.core.config.form.processor.ExtensionOperationStatus;
import ru.intertrust.cm.core.config.form.processor.ExtensionProcessorHelper;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.05.2015
 *         Time: 12:56
 */
public class ExtensionProcessorHelperTest {

    @Test
    public void testReplacingSuccess() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(3);
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(3, "replaced");
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processReplaceConfigs(target, source, operationStatus);
        Assert.assertEquals(3, target.size());
        Assert.assertEquals("replaced1", target.get(0).getContent());
        Assert.assertEquals("replaced2", target.get(1).getContent());
        Assert.assertEquals("replaced3", target.get(2).getContent());
        Assert.assertFalse(operationStatus.isNotSuccessful());

    }

    @Test
    public void testReplacingFailed() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(3);
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(4, "replaced");
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processReplaceConfigs(target, source, operationStatus);
        Assert.assertEquals(3, target.size());
        Assert.assertEquals("replaced1", target.get(0).getContent());
        Assert.assertEquals("replaced2", target.get(1).getContent());
        Assert.assertEquals("replaced3", target.get(2).getContent());
        Assert.assertTrue(operationStatus.isNotSuccessful());
        Assert.assertEquals("Could not replace config with id '4'\n", operationStatus.toErrorString());
    }

    @Test
    public void testAddingBeforeSuccess() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(4, Arrays.asList(1, 2));
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(2);
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processAddConfigsBefore("3", target, source, operationStatus);
        Assert.assertEquals(4, target.size());
        Assert.assertEquals("1", target.get(0).getId());
        Assert.assertEquals("2", target.get(1).getId());
        Assert.assertEquals("3", target.get(2).getId());
        Assert.assertEquals("4", target.get(3).getId());
        Assert.assertFalse(operationStatus.isNotSuccessful());

    }

    @Test
    public void testAddingBeforeFailed() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(4, Arrays.asList(1, 2));
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(2);
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processAddConfigsBefore("2", target, source, operationStatus);
        Assert.assertEquals(2, target.size());
        Assert.assertTrue(operationStatus.isNotSuccessful());
        Assert.assertEquals("Could not add configs before config with id '2'\n", operationStatus.toErrorString());
    }

    @Test
    public void testAddingAfterSuccess() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(4, Arrays.asList(2, 3));
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(3, Arrays.asList(1));
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processAddConfigsAfter("1", target, source, operationStatus);
        Assert.assertEquals(4, target.size());
        Assert.assertEquals("1", target.get(0).getId());
        Assert.assertEquals("2", target.get(1).getId());
        Assert.assertEquals("3", target.get(2).getId());
        Assert.assertEquals("4", target.get(3).getId());
        Assert.assertFalse(operationStatus.isNotSuccessful());

    }

    @Test
    public void testAddingAfterFailed() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(4, Arrays.asList(2, 3));
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(3, Arrays.asList(1));
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processAddConfigsAfter("0", target, source, operationStatus);
        Assert.assertEquals(2, target.size());
        Assert.assertTrue(operationStatus.isNotSuccessful());
        Assert.assertEquals("Could not add configs after config with id '0'\n", operationStatus.toErrorString());
    }

    @Test
    public void testDeleteSuccess() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(4);
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(2);
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processDeleteConfigs(target, source, operationStatus);
        Assert.assertEquals(2, target.size());
        Assert.assertEquals("3", target.get(0).getId());
        Assert.assertEquals("4", target.get(1).getId());
        Assert.assertFalse(operationStatus.isNotSuccessful());

    }

    @Test
    public void testDeleteFailed() {
        List<IdentifiedConfigStub> target = IdentifiedConfigStubCreator.createIdentifiedConfigs(4);
        List<IdentifiedConfigStub> source = IdentifiedConfigStubCreator.createIdentifiedConfigs(5);
        ExtensionOperationStatus operationStatus = new ExtensionOperationStatus();
        ExtensionProcessorHelper.processDeleteConfigs(target, source, operationStatus);
        Assert.assertEquals(0, target.size());
        Assert.assertTrue(operationStatus.isNotSuccessful());
        Assert.assertEquals("Could not delete config with id '5'\n", operationStatus.toErrorString());
    }

}
