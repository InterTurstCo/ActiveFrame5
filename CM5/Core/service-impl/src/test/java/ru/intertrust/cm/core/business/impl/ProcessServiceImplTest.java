package ru.intertrust.cm.core.business.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IOHelpService;
import ru.intertrust.cm.core.business.api.InputStreamProvider;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.workflow.ProcessTemplateInfo;
import ru.intertrust.cm.core.business.api.workflow.WorkflowEngine;
import ru.intertrust.cm.core.model.FatalException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProcessServiceImplTest {

    @Mock
    private WorkflowEngine workflowEngine;
    @Mock
    private CrudService crudService;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private IOHelpService ioHelpService;

    @InjectMocks
    private ProcessServiceImpl processService;

    // DATA FOR TESTS
    private final byte[] processDefinition = "THIS IS A PROCESS DEFINITION".getBytes(StandardCharsets.UTF_8);
    private final String fileName = "Process Definition";
    private final InputStreamProvider inputStreamProvider = () -> new ByteArrayInputStream(processDefinition);

    @Before
    public void init() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            InputStreamProvider isp = (InputStreamProvider) args[0];
            IOUtils.copy(isp.getInputStream(), (ByteArrayOutputStream) args[1]);
            return null;
        }).when(ioHelpService).copyWithEolControl(eq(inputStreamProvider), eq(new ByteArrayOutputStream()));
    }

    @Test (expected = FatalException.class)
    public void saveProcess_null_version() {
        ProcessTemplateInfo info = mock(ProcessTemplateInfo.class);
        when(info.getVersion()).thenReturn(null);
        when(workflowEngine.getProcessTemplateInfo(anyObject())).thenReturn(info);

        processService.setUseCheckSum(false);
        processService.saveProcess(inputStreamProvider, fileName, true);
    }

    @Test
    public void saveProcess_first_upload_without_hash() {
        ProcessTemplateInfo info = mock(ProcessTemplateInfo.class);
        when(info.getVersion()).thenReturn("1.0.0.0");
        when(info.getId()).thenReturn("id");
        when(workflowEngine.getProcessTemplateInfo(any())).thenReturn(info);

        DomainObject processDefinitionObject = mock(DomainObject.class);
        when(crudService.createDomainObject(eq("process_definition"))).thenReturn(processDefinitionObject);
        when(crudService.save(eq(processDefinitionObject))).thenReturn(processDefinitionObject);

        DomainObject attachment = mock(DomainObject.class);
        when(attachmentService.createAttachmentDomainObjectFor(anyObject(), eq("process_definition_model"))).thenReturn(attachment);

        processService.setUseCheckSum(false);
        processService.saveProcess(inputStreamProvider, fileName, false);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(processDefinitionObject).setString(eq("file_name"), nameCaptor.capture());

        ArgumentCaptor<String> attachmentNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(attachment).setString(eq("Name"), attachmentNameCaptor.capture());

        verify(attachmentService, times(1)).saveAttachment(any(), eq(attachment));

        assertEquals(fileName, nameCaptor.getValue());
        assertEquals(fileName, attachmentNameCaptor.getValue());

    }

    @Test(expected = FatalException.class)
    public void saveProcess_first_upload_with_hash() {

        when(ioHelpService.copyWithEolControlAndMd5(eq(inputStreamProvider), any())).thenReturn("MD5 SUM");

        ProcessTemplateInfo info = mock(ProcessTemplateInfo.class);
        when(info.getVersion()).thenReturn("1.0.0.0");
        when(info.getId()).thenReturn("id");
        when(workflowEngine.getProcessTemplateInfo(any())).thenReturn(info);

        DomainObject processDefinitionObject = mock(DomainObject.class);
        when(crudService.findByUniqueKey(eq("process_definition"), any())).thenReturn(processDefinitionObject);
        when(processDefinitionObject.getString(eq("hash"))).thenReturn("Another MD5 SUM");

        processService.setUseCheckSum(true);

        try {
            processService.saveProcess(inputStreamProvider, fileName, false);
        } catch (FatalException ex) {
            assertEquals("Process definition with name " + fileName +
                        " and version 1.0.0.0 already exists.", ex.getMessage());
            throw ex;
        }

    }


}