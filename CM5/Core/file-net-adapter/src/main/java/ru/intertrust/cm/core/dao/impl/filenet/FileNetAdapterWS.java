package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import ru.intertrust.cm.core.dao.impl.filenet.ws.ChangeRequestType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.CheckinAction;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ContentData;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ContentElementResponse;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ContentErrorResponse;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ContentRequestType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ContentResponseType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.CreateAction;
import ru.intertrust.cm.core.dao.impl.filenet.ws.DeleteAction;
import ru.intertrust.cm.core.dao.impl.filenet.ws.DependentObjectType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ElementSpecificationType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ErrorRecordType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ErrorStackType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ExecuteChangesRequest;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ExecuteChangesResponse;
import ru.intertrust.cm.core.dao.impl.filenet.ws.FNCEWS40PortType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.FNCEWS40Service;
import ru.intertrust.cm.core.dao.impl.filenet.ws.FaultResponse;
import ru.intertrust.cm.core.dao.impl.filenet.ws.FilterElementType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.GetContentRequest;
import ru.intertrust.cm.core.dao.impl.filenet.ws.GetContentResponse;
import ru.intertrust.cm.core.dao.impl.filenet.ws.InlineContent;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ListOfObject;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ModifiedPropertiesType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ObjectReference;
import ru.intertrust.cm.core.dao.impl.filenet.ws.ObjectSpecification;
import ru.intertrust.cm.core.dao.impl.filenet.ws.PropertyFilterType;
import ru.intertrust.cm.core.dao.impl.filenet.ws.SingletonObject;
import ru.intertrust.cm.core.dao.impl.filenet.ws.SingletonString;

/**
 * Класс для хранения вложений на сервере filenet
 * @author larin
 * 
 */
public class FileNetAdapterWS implements FileNetAdapter{
    private final static int MAX_FRAGMENT_SIZE = 1000 * 1024;
    
    private String serverUrl;
    private String login;
    private String password;
    private String objectStore;
    private String baseFolder;

    private static FNCEWS40PortType service;

    /**
     * Конструктор
     * @param serverUrl
     * @param login
     * @param password
     * @param objectStore
     * @param baseFolder
     */
    public FileNetAdapterWS(String serverUrl, String login, String password, String objectStore, String baseFolder) {
        this.serverUrl = serverUrl;
        this.login = login;
        this.password = password;
        this.objectStore = objectStore;
        this.baseFolder = baseFolder;
    }

    /**
     * Сохранение вложения
     * @param content
     * @return возвращает путь вложения на сервере filenet. После по этому пути данное вложение можно будет получить
     * @throws FaultResponse
     * @throws IOException
     * @throws SOAPException
     */
    @Override
    public String save(byte[] content) throws Exception {
        UUID uuid = UUID.randomUUID();
        createDocument(uuid.toString(), content);
        return baseFolder + "/" + uuid.toString();
    }

    /**
     * Загрузка вложения по пути на сервере filenet
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public InputStream load(String path) throws Exception {
        File tempFile = File.createTempFile("filenet_", "_temp");
        try(FileOutputStream out = new FileOutputStream(tempFile)) {
            Fragment fragment = null;
            int loadByte = 0;
            while (fragment == null || fragment.fullSize > loadByte) {
                fragment = loadFragment(path, fragment == null ? null : fragment.continueFrom);
                out.write(fragment.body);
                loadByte += fragment.body.length;
            }
        }
        
        TempFileInputStream in = new TempFileInputStream(tempFile);
        return in;
    }
    
    /**
     * Загрузка фрагмента ывложения по пути на сервере filenet
     * @param path
     * @return
     * @throws Exception
     */
    private Fragment loadFragment(String path, String continueFrom) throws Exception {
        Fragment result = new Fragment();
        // Set a reference to the document to retrieve
        ObjectSpecification objDocumentSpec = new ObjectSpecification();
        objDocumentSpec.setClassId("Document");
        objDocumentSpec.setPath(path);
        objDocumentSpec.setObjectStore(objectStore);

        // Construct element specification for GetContent request
        ElementSpecificationType objElemSpecType = new ElementSpecificationType();
        objElemSpecType.setItemIndex(0);
        objElemSpecType.setElementSequenceNumber(0);

        // Construct the GetContent request 
        ContentRequestType objContentReqType = new ContentRequestType();
        objContentReqType.setCacheAllowed(true);
        objContentReqType.setId("1");
        objContentReqType.setMaxBytes(MAX_FRAGMENT_SIZE);
        objContentReqType.setContinueFrom(continueFrom);
        objContentReqType.setElementSpecification(objElemSpecType);
        objContentReqType.setSourceSpecification(objDocumentSpec);

        GetContentRequest objGetContentReq = new GetContentRequest();
        objGetContentReq.getContentRequest().add(objContentReqType);
        objGetContentReq.setValidateOnly(false);

        GetContentResponse objContentRespTypeArray = getService().getContent(objGetContentReq);

        // Process GetContent response
        ContentResponseType objContentRespType = objContentRespTypeArray.getContentResponse().get(0);
        if (objContentRespType instanceof ContentErrorResponse)
        {
            ContentErrorResponse objContentErrorResp = (ContentErrorResponse) objContentRespType;
            ErrorStackType objErrorStackType = objContentErrorResp.getErrorStack();
            ErrorRecordType objErrorRecordType = objErrorStackType.getErrorRecord().get(0);
            String errText =
                    "Error [" + objErrorRecordType.getDescription() + "] occurred. " + " Err source is ["
                            + objErrorRecordType.getSource() + "]";
            throw new Exception(errText);
        }
        else if (objContentRespType instanceof ContentElementResponse)
        {
            ContentElementResponse objContentElemResp = (ContentElementResponse) objContentRespType;
            InlineContent objInlineContent = (InlineContent) objContentElemResp.getContent();

            // Write content to file
            result.body = objInlineContent.getBinary();
            result.fullSize = objContentElemResp.getTotalSize().longValue();
            result.continueFrom = objContentElemResp.getContinueFrom();
        }
        else
        {
            String errText =
                    "Unknown data type returned in content response: [" + objContentRespType.getClass().getName() + "]";
            throw new Exception(errText);
        }
        return result;
    }

    /**
     * Удаление вложения по пути на серере filenet
     * @param path
     * @throws MalformedURLException
     * @throws FaultResponse
     * @throws SOAPException
     */
    @Override
    public void delete(String path) throws Exception {
        // Build the Delete action 
        DeleteAction delAction = new DeleteAction();

        // Assign the action to the ChangeRequestType element 
        ChangeRequestType elemChangeRequestType = new ChangeRequestType();
        elemChangeRequestType.getAction().add(delAction);

        // Set a reference to the document to Delete 
        ObjectSpecification objDocument = new ObjectSpecification();
        objDocument.setClassId("Document");
        objDocument.setPath(path);
        objDocument.setObjectStore(objectStore);

        // Specify the target object (a document) for the actions 
        elemChangeRequestType.setTargetSpecification(objDocument);
        elemChangeRequestType.setId("1");

        // Create a Property Filter to get Reservation property 
        PropertyFilterType elemPropFilter = new PropertyFilterType();
        elemPropFilter.setMaxRecursion(1);
        FilterElementType filterElementType = new FilterElementType();
        filterElementType.setValue("Reservation");
        elemPropFilter.getIncludeProperties().add(filterElementType);

        // Assign the list of included properties to the ChangeRequestType element 
        elemChangeRequestType.setRefreshFilter(elemPropFilter);

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it 
        ExecuteChangesRequest elemExecuteChangesRequest = new ExecuteChangesRequest();
        elemExecuteChangesRequest.getChangeRequest().add(elemChangeRequestType);
        elemExecuteChangesRequest.setRefresh(true); // return a refreshed object 

        getService().executeChanges(elemExecuteChangesRequest);
    }

    /**
     * Получение web сервиса для взаимодействия с filenet
     * @return
     * @throws MalformedURLException
     * @throws SOAPException
     */
    private FNCEWS40PortType getService() throws MalformedURLException, SOAPException {
        if (service == null) {
            FNCEWS40Service adminServiceProxy =
                    new FNCEWS40Service(new URL("https://" + serverUrl + "/wsi/FNCEWS40MTOM"), new QName(
                            "http://www.filenet.com/ns/fnce/2006/11/ws/MTOM/wsdl", "FNCEWS40Service"));
            service = adminServiceProxy.getFNCEWS40MTOMPort();
            BindingProvider bp = (BindingProvider) service;
            Map<String, Object> rc = bp.getRequestContext();
            rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://" + serverUrl + "/wsi/FNCEWS40MTOM");

            List<Handler> handlerChain = new ArrayList<Handler>();
            handlerChain.add(new SecurityHandler(login, password));
            bp.getBinding().setHandlerChain(handlerChain);
            ((SOAPBinding)bp.getBinding()).setMTOMEnabled(true);
        }
        return service;
    }

    /**
     * Создание документа на сервере file net
     * @param name
     * @param content
     * @throws FaultResponse
     * @throws IOException
     * @throws SOAPException
     */
    private void createDocument(String name, byte[] content) throws FaultResponse, IOException, SOAPException {
        // Build the Create action for a document
        CreateAction objCreate = new CreateAction();
        objCreate.setClassId("Document");

        // Build the Checkin action
        CheckinAction objCheckin = new CheckinAction();
        objCheckin.setCheckinMinorVersion(true);

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType objChangeRequestType = new ChangeRequestType();
        objChangeRequestType.getAction().add(objCreate);
        objChangeRequestType.getAction().add(objCheckin);

        // Specify the target object (an object store) for the actions
        objChangeRequestType.setTargetSpecification(new ObjectReference());
        objChangeRequestType.getTargetSpecification().setClassId("ObjectStore");
        objChangeRequestType.getTargetSpecification().setObjectId(objectStore);
        objChangeRequestType.setId("1");

        // Specify and set a string-valued property for the DocumentTitle property
        SingletonString prpDocumentTitle = new SingletonString();
        prpDocumentTitle.setValue(name);
        prpDocumentTitle.setPropertyId("DocumentTitle");
        // Add to property list
        objChangeRequestType.setActionProperties(new ModifiedPropertiesType());
        objChangeRequestType.getActionProperties().getProperty().add(prpDocumentTitle);

        // Create an object reference to dependently persistable ContentTransfer object
        DependentObjectType objContentTransfer = new DependentObjectType();
        objContentTransfer.setClassId("ContentTransfer");
        objContentTransfer.setDependentAction("Insert");

        // Create reference to the object set of ContentTransfer objects returned by the Document.ContentElements property
        ListOfObject prpContentElement = new ListOfObject();
        prpContentElement.setPropertyId("ContentElements");
        prpContentElement.getValue().add(objContentTransfer);

        // Read data stream from file containing the document content
        InlineContent objInlineContent = new InlineContent();
        objInlineContent.setBinary(content);

        // Create reference to Content pseudo-property
        ContentData prpContent = new ContentData();
        prpContent.setValue(objInlineContent);
        prpContent.setPropertyId("Content");

        // Assign Content property to ContentTransfer object 
        objContentTransfer.getProperty().add(prpContent);

        // Create and assign ContentType string-valued property to ContentTransfer object
        SingletonString prpContentType = new SingletonString();
        prpContentType.setPropertyId("ContentType");
        // Set MIME-type to unknown
        prpContentType.setValue("application/unknown");
        objContentTransfer.getProperty().add(prpContentType);
        objChangeRequestType.getActionProperties().getProperty().add(prpContentElement);

        // Assign the list of excluded properties to the ChangeRequestType element
        objChangeRequestType.setRefreshFilter(new PropertyFilterType());
        objChangeRequestType.getRefreshFilter().getExcludeProperties().add("Owner");
        objChangeRequestType.getRefreshFilter().getExcludeProperties().add("DateLastModified");

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest objExecuteChangesRequest = new ExecuteChangesRequest();
        objExecuteChangesRequest.getChangeRequest().add(objChangeRequestType);
        objExecuteChangesRequest.setRefresh(true); // return a refreshed object

        ExecuteChangesResponse responce = getService().executeChanges(objExecuteChangesRequest);
        String id = responce.getChangeResponse().get(0).getObjectId();

        fileDocument(id, name);
    }

    /**
     * Привязка документа на сервере filenet к папке
     * @param documentId
     * @param name
     * @throws FaultResponse
     * @throws MalformedURLException
     * @throws SOAPException
     */
    private void fileDocument(String documentId, String name)
            throws FaultResponse, MalformedURLException, SOAPException {

        // Build the Create action for a document
        CreateAction objCreate = new CreateAction();
        objCreate.setClassId("DynamicReferentialContainmentRelationship");

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType objChangeRequestType = new ChangeRequestType();
        objChangeRequestType.getAction().add(objCreate);

        // Specify the target object (an object store) for the actions
        objChangeRequestType.setTargetSpecification(new ObjectReference());
        objChangeRequestType.getTargetSpecification().setClassId("ObjectStore");
        objChangeRequestType.getTargetSpecification().setObjectId(objectStore);
        objChangeRequestType.setId("1");

        // Specify and set a string-valued property for the ContainmentName property
        SingletonString prpContainmentName = new SingletonString();
        prpContainmentName.setPropertyId("ContainmentName");
        prpContainmentName.setValue(name);
        objChangeRequestType.setActionProperties(new ModifiedPropertiesType());
        objChangeRequestType.getActionProperties().getProperty().add(prpContainmentName);

        // Create an object reference to the document to file
        ObjectReference objDocument = new ObjectReference();
        objDocument.setClassId("Document");
        objDocument.setObjectStore(objectStore);
        objDocument.setObjectId(documentId);

        // Create an object reference to the folder in which to file the document
        ObjectSpecification objFolder = new ObjectSpecification();
        objFolder.setClassId("Folder");
        objFolder.setObjectStore(objectStore);
        objFolder.setPath(baseFolder);

        // Specify and set an object-valued property for the Head property
        SingletonObject prpHead = new SingletonObject();
        prpHead.setPropertyId("Head");
        prpHead.setValue(objDocument); // Set its value to the Document object
        objChangeRequestType.getActionProperties().getProperty().add(prpHead);

        // Specify and set an object-valued property for the Tail property
        SingletonObject prpTail = new SingletonObject();
        prpTail.setPropertyId("Tail");
        prpTail.setValue(objFolder); // Set its value to the folder object
        objChangeRequestType.getActionProperties().getProperty().add(prpTail);

        // Build a list of properties to exclude on the new DRCR object that will be returned
        objChangeRequestType.setRefreshFilter(new PropertyFilterType());
        objChangeRequestType.getRefreshFilter().getExcludeProperties().add("Owner");
        objChangeRequestType.getRefreshFilter().getExcludeProperties().add("DateLastModified");

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest objExecuteChangesRequest = new ExecuteChangesRequest();
        objExecuteChangesRequest.getChangeRequest().add(objChangeRequestType);
        objExecuteChangesRequest.setRefresh(true); // return a refreshed object

        getService().executeChanges(objExecuteChangesRequest);
    }

    private class Fragment{
        private byte[] body;
        private long fullSize;
        private String continueFrom;
    }
    
}
