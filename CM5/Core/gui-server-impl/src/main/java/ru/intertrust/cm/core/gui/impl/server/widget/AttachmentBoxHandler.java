package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.gui.form.widget.AddButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.AttachmentBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.MultiObjectNode;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private AccessVerificationService accessVerificationService;
    @Autowired
    private CrudService crudService;

    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        AttachmentBoxState state = new AttachmentBoxState();
        state.setAttachments(findSelectedAttachments(context));
        state.setAllAttachments(findAllAttachments(context));
        state.setInSelectionMode(isSelectionMode(fieldPath));

        state.setActionLinkConfig(widgetConfig.getActionLinkConfig());
        SelectionStyleConfig selectionStyleConfig = widgetConfig.getSelectionStyle();
        state.setSelectionStyleConfig(selectionStyleConfig);
        state.setAcceptedTypesConfig(widgetConfig.getAcceptedTypesConfig());
        state.setChoiceStyleConfig(widgetConfig.getChoiceStyleConfig());

        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);

        state.setImagesConfig(widgetConfig.getImagesConfig());
        state.setDeleteButtonConfig(prepareDeleteButtonConfig(widgetConfig.getDeleteButtonConfig(), fieldPath));
        state.setAddButtonConfig(prepareAddButtonConfig(widgetConfig.getAddButtonConfig(), fieldPath,
                widgetConfig.getAttachmentType().getName()));
        return state;
    }

    private List<AttachmentItem> findSelectedAttachments(WidgetContext context) {
        List<AttachmentItem> selectedAttachments = new ArrayList<AttachmentItem>();
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        ObjectsNode node = context.getFormObjects().getNode(fieldPath);
        if (node instanceof MultiObjectNode) {
            for (DomainObject object : (MultiObjectNode) node) {
                if (fieldPath.isManyToManyReference()) {
                    String linkedField =  fieldPath.getReferenceName();
                    Id selectedId = object.getReference(linkedField);
                    if (selectedId != null && accessVerificationService.isReadPermitted(selectedId)) {
                        selectedAttachments.add(createAttachmentItem(crudService.find(selectedId)));
                    }
                } else {
                    selectedAttachments.add(createAttachmentItem(object));
                }
            }
        } else if (node instanceof SingleObjectNode){
            DomainObject object = ((SingleObjectNode) node).getDomainObject();
            selectedAttachments.add(createAttachmentItem(object));
        }  else {
            Id selectedId = context.getFieldPlainValue();
            if (selectedId != null && accessVerificationService.isReadPermitted(selectedId)) {
                    selectedAttachments.add(createAttachmentItem(crudService.find(selectedId)));
            }
        }
        return selectedAttachments;
    }

    private List<AttachmentItem> findAllAttachments(WidgetContext context) {
        List<AttachmentItem> allAttachments = new ArrayList<>();

        final FieldPath[] fieldPaths = context.getFieldPaths();
        String[] linkTypes = getLinkedObjectTypes(context, fieldPaths);
        for (int i = 0; i < linkTypes.length; i++) {
            String linkType = linkTypes[i];
            List<DomainObject> domainObjects = crudService.findAll(linkType);
            if (domainObjects != null) {
                for (DomainObject object : domainObjects) {
                    allAttachments.add(createAttachmentItem(object));
                }
            }
        }
        return allAttachments;
    }

    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        List<AttachmentItem> attachmentItems = attachmentBoxState.getAttachments();

        List<DomainObject> newObjects = processAttachmentItems(attachmentItems,
                new ArrayList<DomainObject>(attachmentItems.size()), context, true);

        List<AttachmentItem> newlyAddedAttachments = attachmentBoxState.getNewlyAddedAttachments();
        newlyAddedAttachments.removeAll(attachmentItems); // added but not selected

        newObjects = processAttachmentItems(newlyAddedAttachments, newObjects, context, false);

        if (isSelectionMode(new FieldPath(context.getWidgetConfig().getFieldPathConfig().getValue()))) {
            List<AttachmentItem> deletedAttachments = attachmentBoxState.getNewlyDeletedAttachments();
            for (AttachmentItem deletedAttachment : deletedAttachments) {
                if (deletedAttachment.getId() != null) {
                    attachmentService.deleteAttachment(deletedAttachment.getId());
                }
            }
        }
        return newObjects;
    }

    private List<DomainObject> processAttachmentItems(List<AttachmentItem> attachmentItems, List<DomainObject> newObjects,
                                                      WidgetContext context, boolean setupLinks) {
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();

        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        String attachmentType = widgetConfig.getAttachmentType().getName();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        for (AttachmentItem attachmentItem : attachmentItems) {
            if (attachmentItem.getId() != null) {
                continue;
            }
            String filePath = getFilePath(attachmentItem);
            File fileToSave = new File(filePath);
            long contentLength = fileToSave.length();
            try (InputStream fileData = new FileInputStream(fileToSave);
                 RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(fileData)) {
                DomainObject attachmentDomainObject = createAttachmentDomainObject(attachmentItem, domainObject,
                        attachmentType, filePath, contentLength);

                DomainObject savedDo;
                if (setupLinks) {
                    savedDo = saveAttachment(attachmentDomainObject, domainObject, fieldPath, remoteFileData);
                } else {
                    savedDo = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
                }
                newObjects.add(savedDo);
                fileToSave.delete();
                attachmentItem.setId(savedDo.getId());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newObjects;
    }


    private String getFilePath(AttachmentItem attachmentItem) {
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        return pathForTempFilesStore + attachmentItem.getTemporaryName();
    }

    private DomainObject createAttachmentDomainObject(AttachmentItem attachmentItem, DomainObject parentDomainObject,
                                  String attachmentType, String filePath, long contentLength) throws IOException {
        DomainObject attachmentDomainObject = attachmentService.
                createAttachmentDomainObjectFor(parentDomainObject.getId(), attachmentType);
        attachmentDomainObject.setValue(ATTACHMENT_NAME, new StringValue(attachmentItem.getName()));
        attachmentDomainObject.setValue(ATTACHMENT_DESCRIPTION, new StringValue(attachmentItem.
                getDescription()));
        String mimeType = Files.probeContentType(Paths.get(filePath));
        mimeType = mimeType == null ? "undefined" : mimeType;
        attachmentDomainObject.setValue(ATTACHMENT_MIME_TYPE, new StringValue(mimeType));
        attachmentDomainObject.setValue(ATTACHMENT_CONTENT_LENGTH, new LongValue(contentLength));

        return attachmentDomainObject;
    }

    private DomainObject saveAttachment(DomainObject attachmentDomainObject, DomainObject parentDomainObject,
                                        FieldPath fieldPath, RemoteInputStreamServer remoteFileData) {
        DomainObject savedDo;
        if (fieldPath.isOneToManyReference() || fieldPath.isOneToOneBackReference()) {
            String parentLinkFieldName = fieldPath.getLinkToParentName();
            attachmentDomainObject.setReference(parentLinkFieldName, parentDomainObject);
            savedDo = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
        } else if (fieldPath.isManyToManyReference()) {
            savedDo = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
            String referenceType = fieldPath.getReferenceType();
            DomainObject referencedObject = crudService.createDomainObject(referenceType);
            referencedObject.setReference(fieldPath.getLinkToChildrenName(), savedDo);
            referencedObject.setReference(fieldPath.getLinkToParentName(), parentDomainObject);
            crudService.save(referencedObject);
        } else { // one-to-one reference
            savedDo = attachmentService.saveAttachment(remoteFileData, attachmentDomainObject);
            parentDomainObject.setReference(fieldPath.getFieldName(), savedDo);
            crudService.save(parentDomainObject);
        }
        return  savedDo;
    }

    @Override
    public boolean deleteEntriesOnLinkDrop(WidgetConfig config) {
        FieldPath fieldPath = new FieldPath(config.getFieldPathConfig().getValue());
        return (fieldPath.isOneToManyReference());
    }

    @Override
    //TODO: this method overrides WidgetHandler.isSingleChoice() to return true for isOneToOneBackReference.
    //TODO: Need to analyze if it's applicable for other widgets, and if so, to change the method in  WidgetHandler, and remove this one.
    protected boolean isSingleChoice(WidgetContext context, Boolean singleChoiceFromConfig) {
        FieldPath[] fieldPaths = context.getFieldPaths();
        Boolean singleChoiceAnalyzed = null;
        for (FieldPath fieldPath : fieldPaths) {
            if (singleChoiceAnalyzed != null) {
                if (singleChoiceAnalyzed != (fieldPath.isOneToOneDirectReference() || fieldPath.isField())
                        || fieldPath.isOneToOneBackReference()){
                    throw new GuiException("Multiply fieldPaths should be all reference type or all backreference type");
                }
            }
            singleChoiceAnalyzed = fieldPath.isOneToOneDirectReference() || fieldPath.isField()
                    || fieldPath.isOneToOneBackReference();
        }
        return singleChoiceAnalyzed || (singleChoiceFromConfig != null && singleChoiceFromConfig);
    }

    private AttachmentItem createAttachmentItem(DomainObject object) {
        AttachmentItem attachmentItem = new AttachmentItem();
        attachmentItem.setName(object.getString(ATTACHMENT_NAME));
        attachmentItem.setDescription(object.getString(ATTACHMENT_DESCRIPTION));
        Long contentLength = object.getLong(ATTACHMENT_CONTENT_LENGTH);
        String humanReadableContentLength = GuiUtil.humanReadableByteCount(contentLength);
        attachmentItem.setContentLength(humanReadableContentLength);
        attachmentItem.setId(object.getId());

        return attachmentItem;
    }

    private AddButtonConfig prepareAddButtonConfig(AddButtonConfig config, FieldPath fieldPath, String attachmentTypeName) {
        if (config == null) {
            config = new AddButtonConfig();
            config.setDisplay(!fieldPath.isManyToManyReference());
        }
        config.setDisplay(config.isDisplay() && isAddPermitted(attachmentTypeName));
        return config;
    }

    private DeleteButtonConfig prepareDeleteButtonConfig(DeleteButtonConfig config, FieldPath fieldPath) {
        if (config == null) {
            config = new DeleteButtonConfig();
            config.setDisplay(!fieldPath.isManyToManyReference());
        }
        return config;
    }

    private boolean isAddPermitted(String attachmentTypeName) {
        return accessVerificationService.isCreatePermitted(attachmentTypeName);
    }

    //used for async call from client
    public Dto isDeletePermitted(Dto attachmentId) {
        return new BooleanValue(attachmentId == null || accessVerificationService.isDeletePermitted((Id)attachmentId));
    }

    private boolean isSelectionMode(FieldPath fieldPath) {
        if (fieldPath.isOneToManyReference() || fieldPath.isOneToOneBackReference()) {
            return false;
        }
        return true;
    }
}
