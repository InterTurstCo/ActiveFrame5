package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.model.global.AttachmentUploadTempStorageConfig;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 10:25
 */
@ComponentName("attachment-box")
public class AttachmentBoxHandler  extends LinkEditingWidgetHandler {
    private static final String ATTACHMENT_NAME = "Name";
    private static final String ATTACHMENT_DESCRIPTION = "Description";
    private static final String ATTACHMENT_MIME_TYPE = "MimeType";
    private static final String ATTACHMENT_CONTENT_LENGTH = "ContentLength";

    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        List<AttachmentModel> savedAttachments = new ArrayList<AttachmentModel>();

        MultiObjectNode node = (MultiObjectNode) context.getFormObjects().getNode(fieldPath);
        for (DomainObject object : node) {
            AttachmentModel attachmentModel = new AttachmentModel();
            // todo: в объекте вложения всегда есть поля name и description - не надо их искать, из них просто можно получить значение
             attachmentModel.setName(object.getString(ATTACHMENT_NAME));
             attachmentModel.setDescription(object.getString(ATTACHMENT_DESCRIPTION));
             Long contentLength = object.getLong(ATTACHMENT_CONTENT_LENGTH);
             String humanReadableContentLegth = humanReadableByteCount(contentLength, true);
             attachmentModel.setContentLength(humanReadableContentLegth);
             attachmentModel.setId(object.getId());
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
            // todo Auto-close (Java 7)
            GlobalSettingsConfig globalSettingsConfig = configurationExplorer.getGlobalSettings();
            AttachmentUploadTempStorageConfig attachmentUploadTempStorageConfig= globalSettingsConfig.
                    getAttachmentUploadTempStorageConfig();
            String pathForTempFilesStore = attachmentUploadTempStorageConfig.getPath();
            String filePath = pathForTempFilesStore + attachmentModel.getTemporaryName();
            File fileToSave = new File(filePath);
            long contentLength = fileToSave.length();
            try
                (InputStream fileData = new FileInputStream(fileToSave);
                RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(fileData)) {
                DomainObject attachmentDomainObject = attachmentService.
                        createAttachmentDomainObjectFor(domainObject.getId(),attachmentType);
                attachmentDomainObject.setValue(ATTACHMENT_NAME, new StringValue(attachmentModel.getName()));
                attachmentDomainObject.setValue(ATTACHMENT_DESCRIPTION, new StringValue(attachmentModel.
                        getDescription()));
                String mimeType =  Files.probeContentType(Paths.get(filePath));
                mimeType = mimeType == null ? "undefined" : mimeType;
                attachmentDomainObject.setValue(ATTACHMENT_MIME_TYPE, new StringValue(mimeType));

                attachmentDomainObject.setValue(ATTACHMENT_CONTENT_LENGTH, new LongValue(contentLength));
                attachmentDomainObject.setReference(parentLinkFieldName, domainObject);
                attachmentService.saveAttachment(remoteFileData,attachmentDomainObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
