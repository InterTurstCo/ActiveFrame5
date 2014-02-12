package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.UUID;

import javax.security.auth.Subject;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.util.UserContext;

public class FileNetAdapterJava implements FileNetAdapter{

    private String serverUrl;
    private String login;
    private String password;
    private String objectStore;
    private String baseFolder;
    
    private ObjectStore os;
    
    public FileNetAdapterJava(String serverUrl, String login, String password, String objectStore, String baseFolder) {
        this.serverUrl = serverUrl;
        this.login = login;
        this.password = password;
        this.objectStore = objectStore;
        this.baseFolder = baseFolder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String save(byte[] saveContent) {

        connect();
        
        ////////////////////// Make document
        String fname = UUID.randomUUID().toString();

        Document doc = Factory.Document.createInstance(os, ClassNames.DOCUMENT);
        doc.getProperties().putValue("DocumentTitle", fname);
        
        doc.save(RefreshMode.NO_REFRESH );

        // Check in the document.
        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
        doc.save(RefreshMode.NO_REFRESH);

        // File the document.
        Folder folder = Factory.Folder.getInstance(os, ClassNames.FOLDER, baseFolder);
        ReferentialContainmentRelationship rcr = folder.file(doc, AutoUniqueName.AUTO_UNIQUE, fname, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
        rcr.save(RefreshMode.NO_REFRESH);

        ///////////////////// Add attachment
        // Check out the Document object and save it.
        ByteArrayInputStream bis = new ByteArrayInputStream(saveContent);

        doc.checkout(com.filenet.api.constants.ReservationType.EXCLUSIVE, null, doc.getClassName(), doc.getProperties());
        doc.save(RefreshMode.REFRESH);

        Document reservation = (Document) doc.get_Reservation();
        ContentTransfer ctObject = Factory.ContentTransfer.createInstance();

        @SuppressWarnings("deprecation")
        ContentElementList contentList = Factory.ContentTransfer.createList();      
        ctObject.setCaptureSource(bis);
        ctObject.set_ContentType("application/unknown");

        // Add ContentTransfer object to list.
        contentList.add(ctObject);

        reservation.set_ContentElements(contentList);
        reservation.save(RefreshMode.REFRESH);

        // Check in Reservation object as major version.
        reservation.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
        reservation.save(RefreshMode.REFRESH);

        UserContext.get().popSubject();

//      return baseFolder+reservation.get_Id().toString().replace("{", "/").replace("}", "");
        return baseFolder+"/"+fname;
    }

    @Override
    public InputStream load(String path) {
        connect();
        InputStream stream = null;
    
        Document doc = Factory.Document.fetchInstance(os, path, null );
        
        // Get content elements and iterate list.
        ContentElementList docContentList = doc.get_ContentElements();
        @SuppressWarnings("unchecked")
        Iterator<ContentTransfer> iter = docContentList.iterator();
        
//      while (iter.hasNext() ) {
            ContentTransfer ct = iter.next();
            stream = ct.accessContentStream();
//      }
        
        UserContext.get().popSubject();
        return stream;
    }

    @Override
    public void delete(String path) {
        connect();
        Document doc = Factory.Document.getInstance(os, ClassNames.DOCUMENT, path);
        doc.delete();
        doc.save(RefreshMode.NO_REFRESH);
        doc.delete();
        doc.save(RefreshMode.NO_REFRESH);       
        UserContext.get().popSubject();
    }
    
    private void connect(){
        String uri = "https://"+serverUrl+"/wsi/FNCEWS40MTOM/";
        // Make connection.
        Connection conn = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(conn, login, password, null);
        UserContext.get().pushSubject(subject);
        Domain domain = Factory.Domain.fetchInstance(conn, null, null);
        os = Factory.ObjectStore.getInstance(domain, objectStore);
    }
}
