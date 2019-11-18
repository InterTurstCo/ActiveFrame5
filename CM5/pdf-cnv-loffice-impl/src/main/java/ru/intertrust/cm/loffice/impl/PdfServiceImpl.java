package ru.intertrust.cm.loffice.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.dao.api.ServerComponentService;
import ru.intertrust.cm.core.pdf.PdfService;
import ru.intertrust.cm.core.pdf.PropertiesProvider;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.loffice.exceptions.DefaultPdfConverterException;
import ru.intertrust.cm.loffice.ooo.BootstrapSocketConnector;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 12.05.2016
 * Time: 11:00
 * To change this template use File | Settings | File and Code Templates.
 */
@Stateless
@Local(PdfService.class)
@Remote(PdfService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PdfServiceImpl implements PdfService {

    @Autowired
    private ServerComponentService serverComponentService;


    private static final String OFFICE_PATH_KEY = "default.pdf.converter.lofinstalldir";
    private static final String WORKING_DIR_KEY = "default.pdf.converter.workingdir";
    private static final String CUSTOM_CONVERTER_COMPONENT = "converter.properties.provider";
    private static final String DEFAULT_CONVERTER_COMPONENT = "default.converter.properties.provider";

    private static final String MSG_BOOTSTRAP_ERROR = "Ошибка инициализации компонентов Office. Проверьте настройки.";
    private static final String MSG_CONVERTATION_ERROR = "Ошибка преобразования файла.";
    private static final String PDF_EXTENSION = ".pdf";
    private Boolean initComplete = false;
    private Boolean xDesktopTerminated = false;
    private List<String> initErrors;
    private XDesktop xDesktop;
    private Properties properties;
    private PropertiesProvider provider;

    @Override
    public InputStream getPdf(InputStream sourceStream) throws DefaultPdfConverterException {
        XComponent xComp;
        String tmpFileName;

        if (initComplete) {
            tmpFileName = UUID.randomUUID().toString();
            String tmpFilePath = properties.getProperty(WORKING_DIR_KEY)
                    + System.getProperty("file.separator") + tmpFileName + ".tmp";
            try {
                byte[] buffer = new byte[sourceStream.available()];
                sourceStream.read(buffer);
                File targetFile = new File(tmpFilePath);
                try (OutputStream outStream = new FileOutputStream(targetFile)) {
                    outStream.write(buffer);
                }
                // TODO Поток тут не открываем, соответственно и закрывать здесь некорректно.
                sourceStream.close();
            } catch (java.io.IOException e) {
                throw new DefaultPdfConverterException(String.format(DefaultPdfConverterException.TMP_FILE_CREATION_ERROR, tmpFilePath), e);
            }
            XComponentLoader xCompLoader = (XComponentLoader) UnoRuntime
                    .queryInterface(com.sun.star.frame.XComponentLoader.class, xDesktop);
            String sourceUrl = "file:///" + tmpFilePath;
            PropertyValue[] propertyValues = new PropertyValue[1];
            propertyValues[0] = new PropertyValue();
            propertyValues[0].Name = "Hidden";
            propertyValues[0].Value = new Boolean(true);

            try {
                xComp = xCompLoader.loadComponentFromURL(
                        sourceUrl, "_blank", 0, propertyValues);

                XStorable xStorable = (XStorable) UnoRuntime
                        .queryInterface(XStorable.class, xComp);

                propertyValues = new PropertyValue[2];
                propertyValues[0] = new PropertyValue();
                propertyValues[0].Name = "Overwrite";
                propertyValues[0].Value = new Boolean(true);
                propertyValues[1] = new PropertyValue();
                propertyValues[1].Name = "FilterName";
                propertyValues[1].Value = "writer_pdf_Export";

                String myResult = (properties.getProperty(WORKING_DIR_KEY) + "\\" + tmpFileName + ".pdf").replace("\\","/");
                xStorable.storeToURL("file:///" + myResult, propertyValues);
                xComp.dispose();
                return null;
            } catch (com.sun.star.lang.IllegalArgumentException | com.sun.star.io.IOException e) {
                throw new DefaultPdfConverterException(DefaultPdfConverterException.CONVERTATION_ERROR, e);
            }

        } else {
            throw new DefaultPdfConverterException(DefaultPdfConverterException.XDESKTOP_NOT_INITIALIZED);
        }

    }

    /**
     * Запускаем сервис преобразования. Это тяжеловесный процесс, по этому не должен вызываться
     * в режиме "on demand" а должен создаваться 1 раз определенным процессом обрабатывающим очередь файлов и потом этим же
     * процессом закрываться.
     *
     * @throws DefaultPdfConverterException
     */
    @Override
    public void init() throws DefaultPdfConverterException {
        initErrors = new ArrayList<>();
        try {
            provider = (PropertiesProvider) serverComponentService.getServerComponent(CUSTOM_CONVERTER_COMPONENT);
        } catch (Exception e) {
            provider = (PropertiesProvider) serverComponentService.getServerComponent(DEFAULT_CONVERTER_COMPONENT);
        }
        properties = (Properties) provider.getProperties();

        if (properties.size() == 0) {
            throw new DefaultPdfConverterException(DefaultPdfConverterException.NOT_CONFIGURED_MSG);
        }

        XComponentContext xContext;
        try {
            xContext = BootstrapSocketConnector.bootstrap(properties.getProperty(OFFICE_PATH_KEY));

            XMultiComponentFactory xMCF = xContext.getServiceManager();
            Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);

            xDesktop = UnoRuntime.queryInterface(XDesktop.class, oDesktop);
            initComplete = true;

        } catch (BootstrapException | com.sun.star.uno.Exception e) {
            throw new DefaultPdfConverterException(MSG_BOOTSTRAP_ERROR, e);
        }
    }

    /**
     * Остановка сервиса
     */
    @Override
    public void shutdown() {
        if (xDesktop != null)
            xDesktopTerminated = xDesktop.terminate();
    }

    /**
     * На тот случай если забыли явно вызвать shutdown нужно остановить xDesktop
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        if (!xDesktopTerminated) {
            xDesktop.terminate();
        }
    }
}
