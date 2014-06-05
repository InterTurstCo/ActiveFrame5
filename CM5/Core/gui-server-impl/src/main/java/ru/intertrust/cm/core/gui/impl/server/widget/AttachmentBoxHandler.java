package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.gui.form.widget.AttachmentBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
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
public class AttachmentBoxHandler extends LinkEditingWidgetHandler {
    private static final String ATTACHMENT_NAME = "Name";
    private static final String ATTACHMENT_DESCRIPTION = "Description";
    private static final String ATTACHMENT_MIME_TYPE = "MimeType";
    private static final String ATTACHMENT_CONTENT_LENGTH = "ContentLength";
    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private PropertyResolver propertyResolver;

    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        List<AttachmentItem> savedAttachments = new ArrayList<AttachmentItem>();

        MultiObjectNode node = (MultiObjectNode) context.getFormObjects().getNode(fieldPath);
        for (DomainObject object : node) {
            AttachmentItem attachmentItem = new AttachmentItem();
            attachmentItem.setName(object.getString(ATTACHMENT_NAME));
            attachmentItem.setDescription(object.getString(ATTACHMENT_DESCRIPTION));
            Long contentLength = object.getLong(ATTACHMENT_CONTENT_LENGTH);
            String humanReadableContentLength = GuiUtil.humanReadableByteCount(contentLength);
            attachmentItem.setContentLength(humanReadableContentLength);
            attachmentItem.setId(object.getId());
            savedAttachments.add(attachmentItem);
        }

        AttachmentBoxState state = new AttachmentBoxState();
        state.setActionLinkConfig(widgetConfig.getActionLinkConfig());
        state.setAttachments(savedAttachments);
        SelectionStyleConfig selectionStyleConfig = widgetConfig.getSelectionStyle();
        state.setSelectionStyleConfig(selectionStyleConfig);
        state.setAcceptedTypesConfig(widgetConfig.getAcceptedTypesConfig());
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        return state;
    }

    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        List<AttachmentItem> attachmentItems = attachmentBoxState.getAttachments();
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();

        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        String attachmentType = widgetConfig.getAttachmentType().getName();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        String parentLinkFieldName = fieldPath.getLinkToParentName();

        ArrayList<DomainObject> newObjects = new ArrayList<>(attachmentItems.size());
        for (AttachmentItem attachmentItem : attachmentItems) {
            if (attachmentItem.getId() != null) {
                continue;
            }
            String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
            String filePath = pathForTempFilesStore + attachmentItem.getTemporaryName();
            File fileToSave = new File(filePath);
            long contentLength = fileToSave.length();
            try (InputStream fileData = new FileInputStream(fileToSave);
                 RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(fileData)) {
                DomainObject attachmentDomainObject = attachmentService.
                        createAttachmentDomainObjectFor(domainObject.getId(), attachmentType);
                attachmentDomainObject.setValue(ATTACHMENT_NAME, new StringValue(attachmentItem.getName()));
                attachmentDomainObject.setValue(ATTACHMENT_DESCRIPTION, new StringValue(attachmentItem.
                        getDescription()));
                String mimeType = Files.probeContentType(Paths.get(filePath));
                mimeType = mimeType == null ? "undefined" : mimeType;
                attachmentDomainObject.setValue(ATTACHMENT_MIME_TYPE, new StringValue(mimeType));

                attachmentDomainObject.setValue(ATTACHMENT_CONTENT_LENGTH, new LongValue(contentLength));
                attachmentDomainObject.setReference(parentLinkFieldName, domainObject);
                final DomainObject savedDo = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
                newObjects.add(savedDo);
                fileToSave.delete();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return newObjects;
    }

    @Override
    public boolean deleteEntriesOnLinkDrop(WidgetConfig config) {
        return true;
    }
}
