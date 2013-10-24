package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.AttachmentBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.MultiObjectWidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 10:25
 */
@ComponentName("attachment-box")
public class AttachmentBoxHandler  extends MultiObjectWidgetHandler {
     @Autowired
    AttachmentService attachmentService;
    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        LinkedHashMap<Id, AttachmentModel> savedAttachments = new LinkedHashMap<Id, AttachmentModel>();

        ObjectsNode node = context.getFormObjects().getObjects(fieldPath);
        Iterator<DomainObject> iterator = node.iterator();
        while (iterator.hasNext()) {
            DomainObject temp = iterator.next();
            AttachmentModel attachmentModel = new AttachmentModel();
           for (String field : temp.getFields()) {

                if ("Name".equalsIgnoreCase(field)){
                    attachmentModel.setName(temp.getValue(field).toString());
                }
               if ("Path".equalsIgnoreCase(field)){
                   attachmentModel.setPath(temp.getValue(field).toString());
               }
               if ("MimeType".equalsIgnoreCase(field)){
                   attachmentModel.setMimeType(temp.getValue(field).toString());
               }
               if ("Description".equalsIgnoreCase(field)){
                   attachmentModel.setDescription(temp.getValue(field).toString());
               }
            }
            savedAttachments.put(temp.getId(), attachmentModel);
        }
        AttachmentModel attachmentModel1 = new AttachmentModel();
        attachmentModel1.setName("Some file");
        attachmentModel1.setPath("C:\\attachments\\some_file.pdf");
        AttachmentModel attachmentModel2 = new AttachmentModel();
        attachmentModel2.setName("Other file");
        attachmentModel2.setPath("C:\\attachments\\some_file1.pdf");

        savedAttachments.put(new RdbmsId("1111111111111111"), attachmentModel1);
        savedAttachments.put(new RdbmsId("1111111111111112"), attachmentModel2);
        AttachmentBoxState result = new AttachmentBoxState();
        result.setSavedAttachments(savedAttachments);

      /*  List<AttachmentModel> list = new ArrayList<AttachmentModel>();
        list.add(attachmentModel1);
        list.add(attachmentModel2);
        result.setNewAttachments(list);
        saveNewObjects(context, result); */
        return result;
    }
    public void saveNewObjects(WidgetContext context, WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        List<AttachmentModel> attachmentModels = attachmentBoxState.getNewAttachments();
        DomainObject domainObject = context.getFormObjects().getRootObjects().getObject();

        for (AttachmentModel attachmentModel : attachmentModels) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        String parentLinkFieldName  =  fieldPath.getLastElement().split("\\^")[1];
            System.out.println("----------------parent link " + parentLinkFieldName);
        DomainObject attachmentDomainObject = new GenericDomainObject();
        attachmentDomainObject.setValue("Name", new StringValue(attachmentModel.getName()));
        //attachmentDomainObject.setValue("Path", new StringValue(attachmentModel.getPath()));
        attachmentDomainObject.setValue("MimeType", new StringValue(attachmentModel.getMimeType()));
        attachmentDomainObject.setValue("Description", new StringValue(attachmentModel.getDescription()));
            attachmentDomainObject.setReference(parentLinkFieldName, domainObject );
            try {
                InputStream fileData = new FileInputStream(attachmentModel.getPath());
                RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(fileData);
                attachmentService.saveAttachment(remoteFileData, domainObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
