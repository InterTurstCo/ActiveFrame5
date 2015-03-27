package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentTextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:59
 */
@ComponentName("attachment-text-box")
public class AttachmentTextBoxHandler extends LinkEditingWidgetHandler {
    public static final String ATTACHMENT_ENCODING = "UTF-8";
    @Autowired
    private CrudService crudService;

    @Autowired
    private AttachmentService attachmentService;

    private AttachmentTextState state;

    @Override
    public AttachmentTextState getInitialState(WidgetContext context) {
        final FieldConfig fieldConfig = getFieldConfig(context);
        final Id attachmentId = context.getFieldPlainValue();
        AttachmentTextState state = new AttachmentTextState();;
        if (attachmentId != null) {
            try (final InputStream stream = RemoteInputStreamClient.wrap(attachmentService.loadAttachment(attachmentId));) {
                final String text = IOUtils.toString(stream, ATTACHMENT_ENCODING);
                state.setAttachmentId(attachmentId);
                state.setText(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return state;
    }


    @Override
    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        final AttachmentTextState attachmentTextState = (AttachmentTextState) state;
        if (!attachmentTextState.isChanged()) {
            return null;
        }
        final String fieldName = context.getFirstFieldPath().getFieldName();
        final DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();
        final String attachmentType = ((ReferenceFieldConfig) getFieldConfig(context)).getType();
        final Id attachmentId = attachmentTextState.getAttachmentId();
        final String text = attachmentTextState.getText();
        if (text == null) {
            if (attachmentId == null) {
                return null;
            } else {
                updateDomainObjectReference(context, domainObject, fieldName, null);
                attachmentService.deleteAttachment(attachmentId);
                return null;
            }
        }
        DomainObject attachmentDo;
        if (attachmentId == null) {
            attachmentDo = attachmentService.createAttachmentDomainObjectFor(domainObject.getId(), attachmentType);
            attachmentDo.setString(AttachmentService.NAME, fieldName + getAttachmentExtension());
        } else {
            attachmentDo = crudService.find(attachmentId);
        }
        try (RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(new ByteArrayInputStream(text.getBytes("UTF-8")));) {
            attachmentDo = attachmentService.saveAttachment(remoteFileData, attachmentDo);
            updateDomainObjectReference(context, domainObject, fieldName, attachmentDo.getId());
            return Collections.singletonList(attachmentDo);
        } catch (UnsupportedEncodingException e) { // impossible
            e.printStackTrace();
            return null;
        }
    }

    protected void updateDomainObjectReference(WidgetContext widgetContext, DomainObject domainObject, String field, Id referenceId) {
        domainObject.setReference(field, referenceId);
        final DomainObject savedRoot = crudService.save(domainObject);
        widgetContext.getFormObjects().getRootNode().setDomainObject(savedRoot);
    }

    protected String getAttachmentExtension() {
        return ".txt";
    }
}
