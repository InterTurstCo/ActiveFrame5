package ru.intertrust.cm.core.gui.impl.server.cmd.model;

import org.apache.commons.fileupload.FileItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
@Service("TestPlatformWebService")
public class TestPlatformWebService implements PlatformWebService{

    @Override
    public PlatformWebServiceResult execute(List<FileItem> bytes, Map<String, String[]> data) {
        PlatformWebServiceResult result = new StringPlatformWebServiceResult("Ok");
        return result;
    }
}
