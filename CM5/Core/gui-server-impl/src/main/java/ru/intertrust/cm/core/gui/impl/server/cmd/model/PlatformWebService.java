package ru.intertrust.cm.core.gui.impl.server.cmd.model;

import org.apache.commons.fileupload.FileItem;

import java.util.List;
import java.util.Map;

/**
 * Created by Vitaliy.Orlov on 25.06.2018.
 */
public interface PlatformWebService  {
    PlatformWebServiceResult execute(List<FileItem> bytes, Map<String, String[]> data);
}
