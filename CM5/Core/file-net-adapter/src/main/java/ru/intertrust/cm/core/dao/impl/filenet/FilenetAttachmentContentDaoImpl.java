package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.exception.DaoException;

/**
 * Имплементация AttachmentContentDao для FileNet
 * @author larin
 *
 */
public class FilenetAttachmentContentDaoImpl implements AttachmentContentDao {
    private FileNetAdapter fileNetAdapter = null;

    private String serverUrl;
    private String login;
    private String password;
    private String objectStore;
    private String baseFolder;

    @Override
    public String saveContent(InputStream inputStream) {
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int read = 0;

            while ((read = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
            return getAdapter().save(output.toByteArray());
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
            return getAdapter().load(domainObject.getString("path"));
        } catch (Exception ex) {
            throw new DaoException("Error load content", ex);
        }
    }

    @Override
    public void deleteContent(DomainObject domainObject) {
        try {
            getAdapter().delete(domainObject.getString("path"));
        } catch (Exception ex) {
            throw new DaoException("Error delete content", ex);
        }
    }

    private FileNetAdapter getAdapter() {
        if (fileNetAdapter == null) {
            fileNetAdapter =
                    new FileNetAdapter(getServerUrl(), getLogin(), getPassword(), getObjectStore(), getBaseFolder());
        }
        return fileNetAdapter;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getObjectStore() {
        return objectStore;
    }

    public void setObjectStore(String objectStore) {
        this.objectStore = objectStore;
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }

}
