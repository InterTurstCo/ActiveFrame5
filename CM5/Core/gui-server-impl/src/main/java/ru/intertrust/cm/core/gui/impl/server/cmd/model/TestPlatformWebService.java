package ru.intertrust.cm.core.gui.impl.server.cmd.model;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
@Service("TestPlatformWebService")
public class TestPlatformWebService implements PlatformWebService{
    private static final Logger log = LoggerFactory.getLogger(TestPlatformWebService.class);

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Override
    public PlatformWebServiceResult execute(List<FileItem> files, Map<String, String[]> params) {
        String resultStr = "TestPlatformWebService Ok. ";
        resultStr += " User: " +  currentUserAccessor.getCurrentUser() + "(" + currentUserAccessor.getCurrentUserId().toStringRepresentation() + ")";
        resultStr += " Files: [" +  files.stream().map( item -> item.getFieldName() + "=" + item.getName()).collect(Collectors.joining(";")) + "]";
        resultStr += " Params: [" +  params.entrySet().stream().map( item ->
                item.getKey() + "=[" + Arrays.stream(item.getValue()).collect(Collectors.joining(";")) + "]")
                .collect(Collectors.joining(";")) + "]";

        PlatformWebServiceResult result = new StringPlatformWebServiceResult(resultStr);
        return result;
    }
}
