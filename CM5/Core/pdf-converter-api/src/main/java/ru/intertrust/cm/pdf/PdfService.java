package ru.intertrust.cm.pdf;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 11.05.2016
 * Time: 14:55
 * To change this template use File | Settings | File and Code Templates.
 */
public interface PdfService {
    InputStream getPdf(InputStream source);
}
