package ru.intertrust.cm.core.business.impl.reportgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.impl.ReportResultBuilder;
import ru.intertrust.cm.core.business.impl.report.ReportBuilderFormats;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.service.api.ReportGenerator;

public abstract class XmlToHtmlWithXsltReportGenerator implements ReportGenerator {
    public static final String XSLT_TEMPLATE = "xslt.template";
    @Override
    public InputStream generate(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> parameters) {
        try {
            //Получаем XML с данными
            InputStream xmlData = getXmlData(reportMetadata, templateFolder, parameters);

            //Выполняем Xslt преобразования
            Source xslt = new StreamSource(new File(templateFolder, (String)parameters.get(XSLT_TEMPLATE)));

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Transformer transformer = factory.newTransformer(xslt);

            Source source = new StreamSource(xmlData);
            
            File resultFile = File.createTempFile("xml-to-html-", "-report");
            
            transformer.transform(source, new StreamResult(resultFile));
            // Выводим результат    
            
            return new FileInputStream(resultFile);
        } catch (Exception ex) {
            throw new ReportServiceException("Error generate report", ex);
        }
    }

    public abstract InputStream getXmlData(ReportMetadataConfig reportMetadata, File templateFolder, Map<String, Object> parameters);
    
    @Override
    public String getFormat(){
        return ReportBuilderFormats.HTML_FORMAT.getFormat();
    }

}
