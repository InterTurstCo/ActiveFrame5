package ru.intertrust.cm.loffice.impl;

import ru.intertrust.cm.core.pdf.PdfService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 12.05.2016
 * Time: 11:00
 * To change this template use File | Settings | File and Code Templates.
 */
public class PdfServiceImpl implements PdfService {
    @Override
    public InputStream getPdf(InputStream source) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }
}
