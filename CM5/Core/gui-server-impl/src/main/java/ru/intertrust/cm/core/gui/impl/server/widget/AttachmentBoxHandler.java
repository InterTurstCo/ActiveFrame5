package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.business.api.BaseAttachmentService.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 10:25
 */
@ComponentName("attachment-box")
public class AttachmentBoxHandler extends LinkEditingWidgetHandler {
    private static final String TEMP_STORAGE_PATH_PROPERTY = "${attachment.temp.storage}";
    private static String TEMP_STORAGE_PATH;

    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private PropertyResolver propertyResolver;
    @Autowired
    private AccessVerificationService accessVerificationService;
    @Autowired
    private CrudService crudService;
    @Autowired
    private ConfigurationService configurationService;

    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        AttachmentBoxState state = new AttachmentBoxState();
        state.setAttachments(findSelectedAttachments(context));
        state.setAllAttachments(findSelectedAttachments(context));
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
        state.setClearAllButtonConfig(widgetConfig.getClearButtonConfig());
        state.setDigitalSignaturesConfig(widgetConfig.getDigitalSignaturesConfig());
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
            BusinessUniverseConfig.NAME);
        if(businessUniverseConfig.getBaseUrlConfig()!=null && businessUniverseConfig.getBaseUrlConfig().getValue() != null){
            state.setAppname(businessUniverseConfig.getBaseUrlConfig().getValue());
        }
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

        //CMFIVE-3775
        if(widgetConfig.getAttachmentViewerRefConfig()!=null){
            for(AttachmentItem item : selectedAttachments)
                item.setAttachmentViewerRefConfig((widgetConfig.getAttachmentViewerRefConfig()));
        }

        return selectedAttachments;
    }

    public List<DomainObject> saveNewObjects(WidgetContext context, WidgetState state) {
        List<AttachmentItem> attachmentItems = ((AttachmentBoxState) state).getAttachments();
        DomainObject domainObject = context.getFormObjects().getRootNode().getDomainObject();
        List<DomainObject> newObjects = new ArrayList<>(attachmentItems.size());

        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        String attachmentType = widgetConfig.getAttachmentType().getName();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        for (AttachmentItem attachmentItem : attachmentItems) {
            if (attachmentItem.getId() != null) {
                continue;
            }
            File fileToSave = getTemporaryFile(attachmentItem);
            try (InputStream fileData = new FileInputStream(fileToSave);
                 DirectRemoteInputStream remoteFileData = new DirectRemoteInputStream(fileData, false)) {
                DomainObject attachmentDomainObject = createAttachmentDomainObject(attachmentItem, domainObject, attachmentType);
                DomainObject savedDo = saveAttachment(attachmentDomainObject, domainObject, fieldPath, remoteFileData);
                newObjects.add(savedDo);
                fileToSave.delete();
                attachmentItem.setDomainObjectType(savedDo.getTypeName());
                attachmentItem.setId(savedDo.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return newObjects;
    }

    private File getTemporaryFile(AttachmentItem attachmentItem) {
        return new File(getTemporaryStoragePath(), attachmentItem.getTemporaryName());
    }

    private String getTemporaryStoragePath() {
        if (TEMP_STORAGE_PATH != null) {
            return TEMP_STORAGE_PATH;
        }
        synchronized (AttachmentBoxHandler.class) {
            TEMP_STORAGE_PATH = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH_PROPERTY);
        }
        return TEMP_STORAGE_PATH;
    }

    private DomainObject createAttachmentDomainObject(AttachmentItem attachmentItem, DomainObject parentDomainObject,
                                  String attachmentType) throws IOException {
        DomainObject attachmentDomainObject = attachmentService.createAttachmentDomainObjectFor(parentDomainObject.getId(), attachmentType);
        attachmentDomainObject.setValue(NAME, new StringValue(attachmentItem.getName()));
        attachmentDomainObject.setValue(DESCRIPTION, new StringValue(attachmentItem.getDescription()));
        return attachmentDomainObject;
    }

    private DomainObject saveAttachment(DomainObject attachmentDomainObject, DomainObject parentDomainObject,
                                        FieldPath fieldPath, DirectRemoteInputStream remoteFileData) {
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
                    throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_MULTIPLE_FIELDPATHS,
                            "Multiply fieldPaths should be all reference type or all backreference type",
                            GuiContext.getUserLocale()));
                }
            }
            singleChoiceAnalyzed = fieldPath.isOneToOneDirectReference() || fieldPath.isField()
                    || fieldPath.isOneToOneBackReference();
        }
        return singleChoiceAnalyzed || (singleChoiceFromConfig != null && singleChoiceFromConfig);
    }

    private AttachmentItem createAttachmentItem(DomainObject object) {
        AttachmentItem attachmentItem = new AttachmentItem();
        attachmentItem.setName(object.getString(NAME));
        attachmentItem.setDescription(object.getString(DESCRIPTION));
        attachmentItem.setMimeType(object.getString(MIME_TYPE));
        Long contentLength = object.getLong(CONTENT_LENGTH);
        if (contentLength != null) {
            String humanReadableContentLength = GuiUtil.humanReadableByteCount(contentLength);
            attachmentItem.setContentLength(humanReadableContentLength);
        }
        attachmentItem.setId(object.getId());
        attachmentItem.setDomainObjectType(object.getTypeName());
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
