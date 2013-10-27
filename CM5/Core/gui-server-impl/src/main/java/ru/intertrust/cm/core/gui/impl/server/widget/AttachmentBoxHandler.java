package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.model.gui.form.widget.AttachmentBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 10:25
 */
@ComponentName("attachment-box")
public class AttachmentBoxHandler  extends LinkEditingWidgetHandler {
    private static final String ATTACHMENT_NAME = "Name";
    private static final String ATTACHMENT_DESCRIPTION = "Description";

    @Autowired
    AttachmentService attachmentService;
    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        List<AttachmentModel> savedAttachments = new ArrayList<AttachmentModel>();

        MultiObjectNode node = (MultiObjectNode) context.getFormObjects().getNode(fieldPath);
        for (DomainObject object : node) {
            AttachmentModel attachmentModel = new AttachmentModel();

            // todo: в объекте вложения всегда есть поля name и description - не надо их искать, из них просто можно получить значение
            for (String field : object.getFields()) {
                if (ATTACHMENT_NAME.equalsIgnoreCase(field)) {
                    attachmentModel.setName(object.getValue(field).toString());
                }
                if (ATTACHMENT_DESCRIPTION.equalsIgnoreCase(field)) {
                    attachmentModel.setDescription(object.getValue(field).toString());
                }
                attachmentModel.setId(object.getId());

            }
            savedAttachments.add(attachmentModel);
        }

        AttachmentBoxState result = new AttachmentBoxState();
        result.setAttachments(savedAttachments);

        return result;
    }
    public void saveNewObjects(WidgetContext context, WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        List<AttachmentModel> attachmentModels = attachmentBoxState.getAttachments();
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();

        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        String attachmentType = widgetConfig.getAttachmentType().getName();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        String parentLinkFieldName = fieldPath.getLinkToParentName();

        for (AttachmentModel attachmentModel : attachmentModels) {
            if ( attachmentModel.getId() != null) {
                continue;
            }

            InputStream fileData = null;

            // todo Auto-close (Java 7)
            RemoteInputStreamServer remoteFileData = null;
            try {
                Properties props = PropertiesLoaderUtils.loadAllProperties("deploy.properties");
                String attachmentStorage = props.getProperty("attachment.save.location");
                fileData = new FileInputStream(attachmentStorage + attachmentModel.getTemporaryName());
                remoteFileData = new SimpleRemoteInputStream(fileData);
                DomainObject attachmentDomainObject = attachmentService.
                        createAttachmentDomainObjectFor(domainObject.getId(),attachmentType);
                attachmentDomainObject.setValue(ATTACHMENT_NAME, new StringValue(attachmentModel.getName()));
                attachmentDomainObject.setValue(ATTACHMENT_DESCRIPTION, new StringValue(attachmentModel.
                        getDescription()));

                attachmentDomainObject.setReference(parentLinkFieldName, domainObject );
                attachmentService.saveAttachment(remoteFileData,attachmentDomainObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }  finally {
                close(remoteFileData);

            }

        }
    }
    private  void close(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {

        }
        catch (Exception e) {

        }
    }
}
