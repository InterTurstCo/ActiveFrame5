package ru.intertrust.cm.core.dao.impl;

import org.apache.commons.io.input.UnixLineEndingInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import ru.intertrust.cm.core.business.api.IOHelpService;
import ru.intertrust.cm.core.business.api.InputStreamProvider;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.model.FatalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

@Service
public class IOHelpServiceImpl implements IOHelpService {

    private final MD5Service md5Service;

    @Autowired
    public IOHelpServiceImpl(MD5Service md5Service) {
        this.md5Service = md5Service;
    }

    @Override
    public void copyWithEolControl(InputStreamProvider provider, OutputStream os) {
        try (InputStream is = new UnixLineEndingInputStream(provider.getInputStream(), false)) {
            StreamUtils.copy(is, os);
        } catch (IOException ex) {
            throw new FatalException("Exception occurred during stream copying. Message: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String copyWithEolControlAndMd5(InputStreamProvider provider, OutputStream os) {
        final MessageDigest md = md5Service.newMessageDigest();
        try (InputStream is = new DigestInputStream(new UnixLineEndingInputStream(provider.getInputStream(), false), md)) {
            StreamUtils.copy(is, os);
        } catch (IOException ex) {
            throw new FatalException("Exception occurred during stream copying. Message: " + ex.getMessage(), ex);
        }

        return md5Service.bytesToHex(md.digest());
    }
}
