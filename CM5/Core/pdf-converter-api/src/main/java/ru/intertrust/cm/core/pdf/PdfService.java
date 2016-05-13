package ru.intertrust.cm.core.pdf;

import ru.intertrust.cm.core.pdf.exceptions.ConverterException;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 11.05.2016
 * Time: 14:55
 * To change this template use File | Settings | File and Code Templates.
 */
public interface PdfService {
    public interface Remote extends PdfService {
    }

    InputStream getPdf(InputStream source) throws ConverterException;

    /**
     * Некоторые имплементации сервиса могут протребовать запуска тяжеловесных сторонних процессов
     * таких как например LibreOffice и их последующей остановки. Либо выполнения других пред- или
     * пост- активностей.
     * @throws ConverterException
     */
    void init() throws ConverterException;
    void shutdown();
}
