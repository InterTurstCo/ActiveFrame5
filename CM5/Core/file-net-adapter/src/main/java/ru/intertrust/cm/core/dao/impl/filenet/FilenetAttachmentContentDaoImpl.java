package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;

import com.filenet.api.exception.EngineRuntimeException;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.impl.filenet.ws.FaultResponse;

/**
 * Имплементация AttachmentContentDao для FileNet
 * @author larin
 * 
 */
public class FilenetAttachmentContentDaoImpl implements AttachmentContentDao {

    @Autowired
    private FileNetAdapter fileNetAdapter;

    @Override
    public AttachmentInfo saveContent(InputStream inputStream, DomainObject parentObject, String attachmentType, String fileName) {
        AttachmentInfo attachmentInfo = new AttachmentInfo();
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int read = 0;

            while ((read = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
            String relativePath = fileNetAdapter.save(output.toByteArray());
            attachmentInfo.setRelativePath(relativePath);            
            return attachmentInfo;
        } catch (Exception ex) {
            throw new DaoException("Error save content", ex);
        } finally {
            try {
                output.close();
            } catch (Exception ignoreEx) {
            }
        }
    }

    @Override
    public InputStream loadContent(DomainObject domainObject) {
        try {
            return fileNetAdapter.load(domainObject.getString("path"));
        } catch (Exception ex) {
            throw new DaoException("Error load content", ex);
        }
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        try {
            fileNetAdapter.delete(domainObject.getString("path"));
        }catch(FaultResponse ex){
            if (!ex.getMessage().contains("The requested item was not found")){
                throw new DaoException("Error delete content", ex);
            }
        }catch(EngineRuntimeException ex){
            if (!ex.getMessage().contains("The requested item was not found")){
                throw new DaoException("Error delete content", ex);
            }            
        } catch (Exception ex) {
            throw new DaoException("Error delete content", ex);
        }
    }

    @Override
    public String toRelativeFromAbsPathFile(String absFilePath) {
        throw new UnsupportedOperationException();
    }

}
